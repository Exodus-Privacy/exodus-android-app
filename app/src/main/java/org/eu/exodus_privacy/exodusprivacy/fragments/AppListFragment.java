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
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.databinding.ApplistBinding;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import org.eu.exodus_privacy.exodusprivacy.manager.NetworkManager;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment {


    private PackageManager packageManager;
    private NetworkListener networkListener;
    private ApplicationListAdapter.OnAppClickListener onAppClickListener;
    private boolean startupRefresh;
    private ApplistBinding applistBinding;

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
        applistBinding = DataBindingUtil.inflate(inflater,R.layout.applist,container,false);
        return applistBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(applistBinding == null)
            return;
        Context context = applistBinding.getRoot().getContext();
        applistBinding.swipeRefresh.setOnRefreshListener(() -> startRefresh());
        if (packageManager == null)
            packageManager = context.getPackageManager();

        applistBinding.appList.setLayoutManager(new LinearLayoutManager(context));
        if (packageManager != null) {
            if(startupRefresh) {
                startRefresh();
                startupRefresh = false;
            }
            applistBinding.noPackageManager.setVisibility(View.GONE);
            applistBinding.noAppFound.setVisibility(View.GONE);
            ApplicationListAdapter adapter = new ApplicationListAdapter(packageManager, onAppClickListener);
            if(adapter.getItemCount() == 0) {
                applistBinding.noAppFound.setVisibility(View.VISIBLE);
            } else {
                applistBinding.appList.setAdapter(adapter);
            }
        } else {
            applistBinding.noPackageManager.setVisibility(View.VISIBLE);
        }
    }

    public void startRefresh(){
        if(applistBinding == null)
            return;
        applistBinding.layoutProgress.setVisibility(View.VISIBLE);
        applistBinding.swipeRefresh.setRefreshing(true);
        List<PackageInfo> packageInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        ArrayList<String> packageList = new ArrayList<>();
        for(PackageInfo pkgInfo : packageInstalled)
            packageList.add(pkgInfo.packageName);

        NetworkManager.getInstance().getReports(applistBinding.getRoot().getContext(),networkListener,packageList);
    }

    public void updateComplete() {
        if(applistBinding != null) {
            applistBinding.layoutProgress.setVisibility(View.GONE);
            applistBinding.swipeRefresh.setRefreshing(false);
            if(packageManager != null && applistBinding.appList.getAdapter() != null) {
                ((ApplicationListAdapter) applistBinding.appList.getAdapter()).setPackageManager(packageManager);
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
            if (applistBinding == null)
                return;
            if(maxProgress > 0)
                applistBinding.statusProgress.setText(getString(resourceId)+" "+progress+"/"+maxProgress);
            else
                applistBinding.statusProgress.setText(getString(resourceId));
            applistBinding.progress.setMax(maxProgress);
            applistBinding.progress.setProgress(progress);
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
