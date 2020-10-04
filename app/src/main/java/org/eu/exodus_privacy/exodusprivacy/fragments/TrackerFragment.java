package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.Utils;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.databinding.TrackerBinding;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TrackerFragment extends Fragment implements ComputeAppListTask.Listener, Updatable {

    private TrackerBinding trackerBinding;
    private long trackerId;
    private PackageManager packageManager;
    private List<ApplicationViewModel> applications;
    private AppListFragment appListFragment;
    private ApplicationListAdapter.OnAppClickListener onAppClickListener;

    public static TrackerFragment newInstance(long trackerId) {
        TrackerFragment fragment = new TrackerFragment();
        fragment.setTrackerId(trackerId);
        return fragment;
    }

    private void setTrackerId(long id) {
        trackerId = id;
    }

    @Override
    public void onUpdateComplete() {
        Context context = trackerBinding.getRoot().getContext();
        Tracker tracker = DatabaseManager.getInstance(context).getTracker(trackerId);
        trackerBinding.name.setText(tracker.name);
        trackerBinding.codeDetection.setText(tracker.codeSignature);
        trackerBinding.networkDetection.setText(tracker.networkSignature);
        trackerBinding.description.setText(Html.fromHtml(Utils.markdownToHtml(tracker.description)));
        trackerBinding.description.setMovementMethod(LinkMovementMethod.getInstance());
        trackerBinding.description.setClickable(true);
        trackerBinding.trackerUrl.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(tracker.website));
            startActivity(intent);
        });
        displayAppListAsync();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        trackerBinding = DataBindingUtil.inflate(inflater, R.layout.tracker,container,false);
        if (applications == null)
            applications = new ArrayList<>();
        appListFragment = new AppListFragment();
        appListFragment.setFilter(AppListFragment.Type.TRACKER,trackerId);
        appListFragment.disableScrollBar();
        appListFragment.setOnAppClickListener(onAppClickListener);
        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.applications,appListFragment);
        transaction.commit();
        Context context = trackerBinding.getRoot().getContext();
        packageManager = context.getPackageManager();
        onUpdateComplete();
        return trackerBinding.getRoot();
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_filter);
        item.setVisible(false);
        item = menu.findItem(R.id.action_settings);
        item.setVisible(false);

    }

    private void displayAppListAsync() {
        trackerBinding.noAppFound.setVisibility(View.GONE);
        trackerBinding.trackerPresence.setVisibility(View.GONE);
        //todo
        trackerBinding.trackerPresenceNb.setVisibility(View.GONE);
        //todo
        trackerBinding.trackerPresenceTitle.setVisibility(View.GONE);
        if (applications.isEmpty()) {
            trackerBinding.retrieveApp.setVisibility(View.VISIBLE);
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
        trackerBinding.retrieveApp.setVisibility(View.GONE);
        trackerBinding.noAppFound.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
        trackerBinding.trackerPresence.setVisibility(View.VISIBLE);
        trackerBinding.trackerPresenceNb.setVisibility(View.VISIBLE);
        trackerBinding.trackerPresenceTitle.setVisibility(View.VISIBLE);
        appListFragment.setApplications(apps);
        int total = appListFragment.getTotalApps();
        int displayedApps = appListFragment.getDisplayedApps();
        int percent = displayedApps*100/total;
        if(percent >=50)
            trackerBinding.trackerPresenceNb.setBackgroundResource(R.drawable.square_red);
        else if(percent >=33)
            trackerBinding.trackerPresenceNb.setBackgroundResource(R.drawable.square_dark_orange);
        else if(percent >=20)
            trackerBinding.trackerPresenceNb.setBackgroundResource(R.drawable.square_yellow);
        else
            trackerBinding.trackerPresenceNb.setBackgroundResource(R.drawable.square_light_blue);

        trackerBinding.trackerPresenceNb.setText(percent+"%");
        Context context = trackerBinding.getRoot().getContext();
        String presence = context.getResources().getString(R.string.tracker_presence,displayedApps);
        trackerBinding.trackerPresence.setText(presence);
        trackerBinding.trackerPresenceTitle.setText(R.string.tracker_presence_in);
    }

    public void setOnAppClickListener(ApplicationListAdapter.OnAppClickListener listener) {
        onAppClickListener = listener;
    }
}
