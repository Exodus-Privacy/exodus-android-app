
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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.PermissionListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.TrackerListAdapter;
import org.eu.exodus_privacy.exodusprivacy.databinding.ReportBinding;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Permission;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReportFragment  extends Fragment {

    private PackageManager packageManager;
    private PackageInfo packageInfo = null;
    private ReportBinding reportBinding;

    public static ReportFragment newInstance(PackageManager packageManager, PackageInfo packageInfo) {
        ReportFragment fragment = new ReportFragment();
        fragment.setPackageManager(packageManager);
        fragment.setPackageInfo(packageInfo);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && packageInfo == null) {
            packageInfo = savedInstanceState.getParcelable("PackageInfo");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("PackageInfo", packageInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        reportBinding = DataBindingUtil.inflate(inflater,R.layout.report,container,false);
        updateComplete();
        return reportBinding.getRoot();
    }

    public void updateComplete() {
        Context context = reportBinding.getRoot().getContext();
        String packageName = packageInfo.packageName;
        String versionName = packageInfo.versionName;
        long versionCode = packageInfo.versionCode;

        //setup logo
        try {
            reportBinding.logo.setImageDrawable(packageManager.getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //setup name
        reportBinding.name.setText(packageManager.getApplicationLabel(packageInfo.applicationInfo));

        //setup permissions number
        if (packageInfo.requestedPermissions != null) {
            reportBinding.permissionsNb.setText(String.valueOf(packageInfo.requestedPermissions.length));
            reportBinding.permissionsNb.setVisibility(View.VISIBLE);
        }
        else {
            reportBinding.permissionsNb.setVisibility(View.GONE);
        }

        if(packageInfo.requestedPermissions != null){
            if(packageInfo.requestedPermissions.length == 0)
                reportBinding.permissionsNb.setBackgroundResource(R.drawable.square_green);
            else if(packageInfo.requestedPermissions.length < 5)
                reportBinding.permissionsNb.setBackgroundResource(R.drawable.square_yellow);
            else
                reportBinding.permissionsNb.setBackgroundResource(R.drawable.square_red);
        }

        //setup permissions list
        List<Permission> requestedPermissions = null;
        if (packageInfo.requestedPermissions != null && packageInfo.requestedPermissions.length > 0) {
            requestedPermissions = new ArrayList<>();
            for(int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                Permission permission = new Permission();
                permission.fullName = packageInfo.requestedPermissions[i];
                try {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission.fullName,PackageManager.GET_META_DATA);
                    if(permissionInfo.loadDescription(packageManager) != null)
                        permission.description = permissionInfo.loadDescription(packageManager).toString();
                    if(permissionInfo.loadLabel(packageManager) != null)
                        permission.name = permissionInfo.loadLabel(packageManager).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                requestedPermissions.add(permission);
            }
        }

        reportBinding.permissions.setLayoutManager(new LinearLayoutManager(context));
        PermissionListAdapter permissionAdapter = new PermissionListAdapter(requestedPermissions);
        reportBinding.permissions.setNestedScrollingEnabled(false);
        reportBinding.permissions.setAdapter(permissionAdapter);


        reportBinding.analysed.setVisibility(View.GONE);
        reportBinding.trackerLayout.setVisibility(View.VISIBLE);

        //get trackers
        Report report = null;
        if(versionName != null)
            report = DatabaseManager.getInstance(context).getReportFor(packageName,versionName);
        else
            report = DatabaseManager.getInstance(context).getReportFor(packageName,versionCode);
        Set<Tracker> trackers = null;
        if(report != null) {
            trackers = DatabaseManager.getInstance(context).getTrackers(report.trackers);
        } else {
            reportBinding.analysed.setVisibility(View.VISIBLE);
            reportBinding.trackerLayout.setVisibility(View.GONE);
        }
        //setup trackers report
        if(trackers != null) {
            reportBinding.trackersNb.setText(String.valueOf(trackers.size()));
            reportBinding.trackersNb.setVisibility(View.VISIBLE);
        } else {
            reportBinding.trackersNb.setVisibility(View.GONE);
        }
        if(trackers != null){
            if(trackers.size() == 0)
                reportBinding.trackersNb.setBackgroundResource(R.drawable.square_green);
            else if(trackers.size() < 5)
                reportBinding.trackersNb.setBackgroundResource(R.drawable.square_yellow);
            else
                reportBinding.trackersNb.setBackgroundResource(R.drawable.square_red);
        }

        //setup trackers lists
        reportBinding.trackers.setLayoutManager(new LinearLayoutManager(context));
        TrackerListAdapter trackerAdapter = new TrackerListAdapter(trackers,R.layout.tracker_item);
        reportBinding.trackers.setNestedScrollingEnabled(false);
        reportBinding.trackers.setAdapter(trackerAdapter);

        //setup creator
        if(report != null)
            reportBinding.creator.setText(DatabaseManager.getInstance(context).getCreator(report.appId));
        else
            reportBinding.creator.setVisibility(View.GONE);

        //setup installed
        String installed_str = "";
        if(versionName != null)
            installed_str = context.getString(R.string.installed) +" "+ versionName;
        else
            installed_str = context.getString(R.string.installed) +" "+ String.valueOf(versionCode);
        reportBinding.installedVersion.setText(installed_str);

        //setup reportversion
        reportBinding.reportVersion.setVisibility(View.VISIBLE);
        if(report != null) {
            String report_str = "";
            if (versionName != null && !report.version.equals(versionName)) {
                report_str = context.getString(R.string.report_version) + " " + report.version;
            } else if (versionName == null && report.versionCode != versionCode) {
                report_str = context.getString(R.string.report_version) + " " + report.versionCode;
            }
            if(!report_str.isEmpty())
                reportBinding.reportVersion.setText(report_str);
            else
                reportBinding.reportVersion.setVisibility(View.GONE);
        }
        else
            reportBinding.reportVersion.setVisibility(View.GONE);

        //setup report url
        if(report != null)
            reportBinding.reportUrl.setText("https://reports.exodus-privacy.eu.org/reports/"+report.id+"/");
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_filter);
        item.setVisible(false);
    }
}
