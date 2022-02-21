package org.eu.exodus_privacy.exodusprivacy.fragments;
/*
 * Copyright (C) 2020  Thomas Schneider
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.adapters.MyTrackersListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.TrackerListAdapter;
import org.eu.exodus_privacy.exodusprivacy.databinding.MyTrackersBinding;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.objects.MyTracker;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class MyTrackersFragment extends Fragment implements MyTrackersListAdapter.TrackerClickListener {

    private Context context;
    private MyTrackersBinding trackerBinding;
    private TrackerListAdapter.OnTrackerClickListener onTrackerClickListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        trackerBinding = MyTrackersBinding.inflate(LayoutInflater.from(context));
        return trackerBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        trackerBinding.loader.setVisibility(View.VISIBLE);
        trackerBinding.trackers.setVisibility(View.GONE);
        trackerBinding.swipeRefresh.setOnRefreshListener(this::refresh);
        trackerBinding.refresh.setOnClickListener(v -> refresh());
        refresh();

    }


    private void refresh() {
        new Thread(() -> {
            DatabaseManager databaseManager = DatabaseManager.getInstance(getActivity());
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> packageInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            List<MyTracker> myTrackers = new ArrayList<>();
            List<String> added = new ArrayList<>();
            int maxValue = 0;

            List<ApplicationViewModel> vms = ComputeAppList.compute(packageManager, DatabaseManager.getInstance(getActivity()), null);
            int appInstalled = vms.size();
            for (PackageInfo pkgInfo : packageInstalled) {
                Report report;
                if (pkgInfo.versionName != null)
                    report = databaseManager.getReportFor(pkgInfo.packageName, pkgInfo.versionName, null);
                else {
                    report = databaseManager.getReportFor(pkgInfo.packageName, PackageInfoCompat.getLongVersionCode(pkgInfo), null);
                }
                if (report != null) {
                    Set<Tracker> trackersApp = databaseManager.getTrackers(report.trackers);
                    for (Tracker tracker : trackersApp) {
                        if (added.contains(tracker.codeSignature)) {
                            for (MyTracker myTracker : myTrackers) {
                                if (myTracker.signature.compareTo(tracker.codeSignature) == 0) {
                                    myTracker.number += 1;
                                }
                            }
                        } else {
                            MyTracker myTracker = new MyTracker();
                            myTracker.signature = tracker.codeSignature;
                            myTracker.number = 1;
                            myTracker.tracker = tracker;
                            myTrackers.add(myTracker);
                            added.add(myTracker.signature);
                        }
                    }
                }
            }
            for (MyTracker myTracker : myTrackers) {
                if (myTracker.number > maxValue)
                    maxValue = myTracker.number;
            }
            Handler mainHandler = new Handler(Looper.getMainLooper());
            int finalMaxValue = maxValue;
            Runnable myRunnable = () -> {
                if (myTrackers.size() > 0) {
                    Collections.sort(myTrackers, (obj1, obj2) -> Integer.compare(obj2.number, obj1.number));
                    MyTrackersListAdapter myTrackersListAdapter = new MyTrackersListAdapter(myTrackers, MyTrackersFragment.this, finalMaxValue, appInstalled);
                    trackerBinding.trackers.setAdapter(myTrackersListAdapter);
                    trackerBinding.trackers.setLayoutManager(new LinearLayoutManager(context));
                    trackerBinding.trackers.setVisibility(View.VISIBLE);
                    trackerBinding.loader.setVisibility(View.GONE);
                    trackerBinding.refresh.setVisibility(View.GONE);
                } else {
                    trackerBinding.refresh.setVisibility(View.VISIBLE);
                    trackerBinding.trackers.setVisibility(View.GONE);
                    trackerBinding.loader.setVisibility(View.GONE);
                }
                trackerBinding.swipeRefresh.setRefreshing(false);
            };
            mainHandler.post(myRunnable);


        }).start();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_filter_options).setVisible(false);
    }

    public void setOnTrackerClickListener(TrackerListAdapter.OnTrackerClickListener listener) {
        onTrackerClickListener = listener;
    }

    @Override
    public void onTrackerClick(long trackerId) {
        onTrackerClickListener.onTrackerClick(trackerId);
    }
}
