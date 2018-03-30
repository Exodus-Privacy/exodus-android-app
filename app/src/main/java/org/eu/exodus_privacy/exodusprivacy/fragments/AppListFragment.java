/*
 * Copyright (C) 2018  Anthony Chomienne, anthony@mob-dev.fr
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

package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import org.eu.exodus_privacy.exodusprivacy.manager.NetworkManager;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment {


    private PackageManager packageManager;
    private NetworkListener networkListener;
    private ApplicationListAdapter.OnAppClickListener onAppClickListener;
    private boolean startupRefresh;

    public static AppListFragment newInstance(NetworkListener networkListener, ApplicationListAdapter.OnAppClickListener appClickListener) {
        AppListFragment fragment = new AppListFragment();
        fragment.setNetworkListener(networkListener);
        fragment.setOnAppClickListener(appClickListener);
        fragment.startupRefresh = true;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.applist,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        if(v == null)
            return;
        RecyclerView app_list = v.findViewById(R.id.app_list);
        SwipeRefreshLayout refresh = v.findViewById(R.id.swipe_refresh);
        refresh.setOnRefreshListener(() -> startRefresh(getView()));
        if (packageManager == null)
            packageManager = v.getContext().getPackageManager();

        app_list.setLayoutManager(new LinearLayoutManager(v.getContext()));
        TextView nopm = v.findViewById(R.id.no_package_manager);
        TextView noappfound = v.findViewById(R.id.no_app_found);
        if (packageManager != null) {
            if(startupRefresh) {
                startRefresh(v);
                startupRefresh = false;
            }
            nopm.setVisibility(View.GONE);
            noappfound.setVisibility(View.GONE);
            ApplicationListAdapter adapter = new ApplicationListAdapter(packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS), packageManager, onAppClickListener);
            if(adapter.getItemCount() == 0) {
                noappfound.setVisibility(View.VISIBLE);
            } else {
                app_list.setAdapter(adapter);
            }
        } else {
            nopm.setVisibility(View.VISIBLE);
        }
    }

    public void startRefresh(View v){
        if(v == null)
            return;
        LinearLayout layout = v.findViewById(R.id.layout_progress);
        layout.setVisibility(View.VISIBLE);
        SwipeRefreshLayout refresh = v.findViewById(R.id.swipe_refresh);
        refresh.setRefreshing(true);
        List<PackageInfo> packageInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        ArrayList<String> packageList = new ArrayList<>();
        for(PackageInfo pkgInfo : packageInstalled)
            packageList.add(pkgInfo.packageName);

        NetworkManager.getInstance().getReports(v.getContext(),networkListener,packageList);
    }

    public void updateComplete() {
        View v = getView();
        if(v != null) {
            LinearLayout layout = v.findViewById(R.id.layout_progress);
            layout.setVisibility(View.GONE);
            SwipeRefreshLayout refresh = v.findViewById(R.id.swipe_refresh);
            refresh.setRefreshing(false);
            RecyclerView app_list = v.findViewById(R.id.app_list);
            if(packageManager != null && app_list.getAdapter() != null) {
                ((ApplicationListAdapter) app_list.getAdapter()).setPackageManager(packageManager);
                app_list.getAdapter().notifyDataSetChanged();
            }
        }
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
        Activity activity = getActivity();
        if(activity == null)
            return;
        activity.runOnUiThread(() -> {
            View v = getView();
            if (v == null)
                return;
            TextView status = v.findViewById(R.id.status_progress);
            if(maxProgress > 0)
                status.setText(getString(resourceId)+" "+progress+"/"+maxProgress);
            else
                status.setText(getString(resourceId));
            ProgressBar progressBar = v.findViewById(R.id.progress);
            progressBar.setMax(maxProgress);
            progressBar.setProgress(progress);
        });

    }

    public void setOnAppClickListener(ApplicationListAdapter.OnAppClickListener onAppClickListener) {
        this.onAppClickListener = onAppClickListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        packageManager = context.getPackageManager();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        packageManager = null;
    }
}
