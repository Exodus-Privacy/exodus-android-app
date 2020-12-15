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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.MyTrackersListAdapter;
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
        Context context = trackerBinding.getRoot().getContext();
        trackerBinding.loader.setVisibility(View.VISIBLE);
        trackerBinding.trackers.setVisibility(View.GONE);
        new Thread(() -> {
            DatabaseManager databaseManager = DatabaseManager.getInstance(getActivity());
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> packageInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            List<MyTracker> myTrackers = new ArrayList<>();
            List<String> added = new ArrayList<>();
            for (PackageInfo pkgInfo : packageInstalled) {
                Report report;
                if (pkgInfo.versionName != null)
                    report = databaseManager.getReportFor(pkgInfo.packageName, pkgInfo.versionName, null);
                else {
                    report = databaseManager.getReportFor(pkgInfo.packageName, pkgInfo.versionCode, null);
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
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = () -> {
                Collections.sort(myTrackers, (obj1, obj2) -> Integer.compare(obj2.number, obj1.number));
                MyTrackersListAdapter myTrackersListAdapter = new MyTrackersListAdapter(myTrackers, MyTrackersFragment.this);
                trackerBinding.trackers.setAdapter(myTrackersListAdapter);
                trackerBinding.trackers.setLayoutManager(new LinearLayoutManager(context));
                trackerBinding.trackers.setVisibility(View.VISIBLE);
                trackerBinding.loader.setVisibility(View.GONE);
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
        menu.findItem(R.id.action_filter).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_filter_options).setVisible(false);
    }


    @Override
    public void onTrackerClick(long trackerId) {

    }
}
