package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.databinding.HomeBinding;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.manager.NetworkManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ComputeAppListTask.Listener, Updatable {

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

    private int lastResource=0;
    private int lastProgress=0;
    private int lastMaxProgress=0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(applications == null)
            applications = new ArrayList<>();
        homeBinding = DataBindingUtil.inflate(inflater, R.layout.home,container,false);
        appListFragment = new AppListFragment();
        appListFragment.setOnAppClickListener(onAppClickListener);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.app_list_container,appListFragment);
        transaction.commit();
        Context context = homeBinding.getRoot().getContext();
        packageManager = context.getPackageManager();
        homeBinding.swipeRefresh.setOnRefreshListener(this::startRefresh);
        if(packageManager != null) {
            homeBinding.noPackageManager.setVisibility(View.GONE);
            onAppsComputed(applications);
            if(applications.isEmpty())
                displayAppListAsync();
            if(startRefreshAsked)
                startRefresh();
            else if (refreshInProgress) {
                homeBinding.layoutProgress.setVisibility(View.VISIBLE);
                homeBinding.swipeRefresh.setRefreshing(true);
                updateProgress(lastResource,lastProgress,lastMaxProgress);
            }
        } else {
            homeBinding.noPackageManager.setVisibility(View.VISIBLE);
        }
        return homeBinding.getRoot();
    }

    public void startRefresh(){
        if(packageManager != null) {
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
    public void onUpdateComplete() {
        refreshInProgress = false;
        homeBinding.layoutProgress.setVisibility(View.GONE);
        homeBinding.swipeRefresh.setRefreshing(false);
        displayAppListAsync();
    }

    public void setNetworkListener(NetworkListener listener) {
        this.networkListener = new NetworkListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
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
        if(lastResource == 0)
            return;
        Activity activity = getActivity();
        if(activity == null)
            return;
        activity.runOnUiThread(() -> {
            if (homeBinding == null)
                return;
            if(maxProgress > 0)
                homeBinding.statusProgress.setText(activity.getString(resourceId)+" "+progress+"/"+maxProgress);//fixme
            else
                homeBinding.statusProgress.setText(activity.getString(resourceId));
            homeBinding.progress.setMax(maxProgress);
            homeBinding.progress.setProgress(progress);
        });

    }

    public void setOnAppClickListener(ApplicationListAdapter.OnAppClickListener onAppClickListener) {
        this.onAppClickListener = onAppClickListener;
        if(appListFragment != null)
            appListFragment.setOnAppClickListener(onAppClickListener);
    }

    public void filter(String filter){
        appListFragment.setFilter(AppListFragment.Type.NAME,filter);
    }

    private void displayAppListAsync() {
        homeBinding.noAppFound.setVisibility(View.GONE);
        if (applications.isEmpty()) {
            homeBinding.retrieveApp.setVisibility(View.VISIBLE);
            homeBinding.logo.setVisibility(View.VISIBLE);
        }

        new ComputeAppListTask(
                new WeakReference<>(packageManager),
                new WeakReference<>(DatabaseManager.getInstance(getActivity())),
                new WeakReference<>(this)
        ).execute();
    }

    @Override
    public void onAppsComputed(List<ApplicationViewModel> apps) {
        this.applications = apps;
        homeBinding.retrieveApp.setVisibility(View.GONE);
        homeBinding.logo.setVisibility(View.GONE);
        homeBinding.noAppFound.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
        appListFragment.setApplications(apps);
        if(!apps.isEmpty()) {
            if(startupRefresh) {
                startRefresh();
                startupRefresh = false;
            }
        }
    }
}
