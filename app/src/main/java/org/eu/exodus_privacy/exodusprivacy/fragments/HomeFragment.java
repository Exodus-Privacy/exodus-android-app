package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.Utils;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.databinding.HomeBinding;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.manager.NetworkManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Application;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements ComputeAppList.Listener, Updatable {

    private @Nullable
    PackageManager packageManager;
    private NetworkListener networkListener;
    private ApplicationListAdapter.OnAppClickListener onAppClickListener;
    private boolean startupRefresh = true;
    private HomeBinding homeBinding;
    private List<ApplicationViewModel> applications;
    private AppListFragment appListFragment;
    private boolean startRefreshAsked;
    private boolean refreshInProgress;

    private int lastResource = 0;
    private int lastProgress = 0;
    private int lastMaxProgress = 0;
    private String last_refresh;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (applications == null)
            applications = new ArrayList<>();
        homeBinding = DataBindingUtil.inflate(inflater, R.layout.home, container, false);
        appListFragment = new AppListFragment();
        appListFragment.setOnAppClickListener(onAppClickListener);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.app_list_container, appListFragment);
        transaction.commit();
        Context context = homeBinding.getRoot().getContext();
        packageManager = context.getPackageManager();
        homeBinding.swipeRefresh.setOnRefreshListener(this::startRefresh);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.APP_PREFS, MODE_PRIVATE);
        last_refresh = sharedPreferences.getString(Utils.LAST_REFRESH, null);

        if (packageManager != null) {
            homeBinding.noPackageManager.setVisibility(View.GONE);

            onAppsComputed(applications);
            if (applications.isEmpty())
                displayAppListAsync(null);
            if (startRefreshAsked && last_refresh == null)
                startRefresh();
            else if (refreshInProgress) {
                homeBinding.layoutProgress.setVisibility(View.VISIBLE);
                homeBinding.swipeRefresh.setRefreshing(true);
                updateProgress(lastResource, lastProgress, lastMaxProgress);
            }

        } else {
            homeBinding.noPackageManager.setVisibility(View.VISIBLE);
        }
        return homeBinding.getRoot();
    }

    public void startRefresh() {
        if (packageManager != null) {
            refreshInProgress = true;
            homeBinding.layoutProgress.setVisibility(View.VISIBLE);
            homeBinding.swipeRefresh.setRefreshing(true);
            List<PackageInfo> packageInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            ArrayList<String> packageList = new ArrayList<>();
            for (PackageInfo pkgInfo : packageInstalled)
                packageList.add(pkgInfo.packageName);

            NetworkManager.getInstance().getReports(homeBinding.getRoot().getContext(), networkListener, packageList);
            startRefreshAsked = false;
        } else {
            startRefreshAsked = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        appListFragment.scrollTo();
    }


    @Override
    public void onUpdateComplete() {
        refreshInProgress = false;
        homeBinding.layoutProgress.setVisibility(View.GONE);
        homeBinding.swipeRefresh.setRefreshing(false);
        displayAppListAsync(null);
    }

    public void setNetworkListener(NetworkListener listener) {
        this.networkListener = new NetworkListener() {
            @Override
            public void onSuccess(Application application) {
                listener.onSuccess(application);
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }

            public void onProgress(int resourceId, int progress, int maxProgress) {
                updateProgress(resourceId, progress, maxProgress);
            }
        };
    }

    private void updateProgress(int resourceId, int progress, int maxProgress) {
        lastResource = resourceId;
        lastProgress = progress;
        lastMaxProgress = maxProgress;
        if (lastResource == 0)
            return;
        Activity activity = getActivity();
        if (activity == null)
            return;
        activity.runOnUiThread(() -> {
            if (homeBinding == null)
                return;
            if (maxProgress > 0)
                homeBinding.statusProgress.setText(String.format(Locale.getDefault(), "%s %d/%d", activity.getString(resourceId), progress, maxProgress));
            else
                homeBinding.statusProgress.setText(activity.getString(resourceId));
            homeBinding.progress.setMax(maxProgress);
            homeBinding.progress.setProgress(progress);
        });

    }

    public void setOnAppClickListener(ApplicationListAdapter.OnAppClickListener onAppClickListener) {
        this.onAppClickListener = onAppClickListener;
        if (appListFragment != null)
            appListFragment.setOnAppClickListener(onAppClickListener);
    }

    public void filter(String filter) {
        appListFragment.setFilter(AppListFragment.Type.NAME, filter);
    }

    public void displayAppListAsync(ComputeAppList.order orderList) {
        homeBinding.noAppFound.setVisibility(View.GONE);
        if (applications.isEmpty()) {
            homeBinding.retrieveApp.setVisibility(View.VISIBLE);
            homeBinding.logo.setVisibility(View.VISIBLE);
        }

        new Thread(() -> {
            List<ApplicationViewModel> vms = ComputeAppList.compute(packageManager, DatabaseManager.getInstance(getActivity()), orderList);
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = () -> onAppsComputed(vms);
            mainHandler.post(myRunnable);
        }).start();
    }

    @Override
    public void onAppsComputed(List<ApplicationViewModel> apps) {
        this.applications = apps;
        homeBinding.retrieveApp.setVisibility(View.GONE);
        homeBinding.logo.setVisibility(View.GONE);
        homeBinding.noAppFound.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
        appListFragment.setApplications(apps);
        if (!apps.isEmpty()) {
            if (startupRefresh) {
                Calendar cal = Calendar.getInstance();
                if (last_refresh != null) {
                    cal.setTime(Utils.stringToDate(getContext(), last_refresh));
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
                Date refreshAfter = cal.getTime();
                Date currentDate = new Date();
                if (last_refresh != null && !refreshInProgress && currentDate.after(refreshAfter)) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setMessage(getString(R.string.refresh_needed_message, last_refresh));
                    dialogBuilder.setPositiveButton(R.string.refresh, (dialog, id) -> {
                        startRefresh();
                        dialog.dismiss();
                    });
                    dialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();

                } else if (last_refresh == null) {
                    startRefresh();
                }
                startupRefresh = false;
            }
        }
    }
}
