
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

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.ReportBinding;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ReportFragment  extends Fragment {

    private PackageManager packageManager;
    private PackageInfo packageInfo;
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

        //setup logo
        try {
            reportBinding.logo.setImageDrawable(packageManager.getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //setup name
        reportBinding.name.setText(packageManager.getApplicationLabel(packageInfo.applicationInfo));

        //setup permissions number
        String permissions_text;
        if (packageInfo.requestedPermissions != null && packageInfo.requestedPermissions.length > 0)
            permissions_text = context.getString(R.string.permissions) + " " + String.valueOf(packageInfo.requestedPermissions.length);
        else
            permissions_text = context.getString(R.string.permissions);

        reportBinding.permissionsTitle.setText(permissions_text);

        //setup permissions list
        //Build html permissions list
        if (packageInfo.requestedPermissions != null && packageInfo.requestedPermissions.length > 0) {
            List<String> requestedPermissions = Arrays.asList(packageInfo.requestedPermissions);
            String html = buildHtmlList(requestedPermissions);
            reportBinding.permissions.loadData(html,"text/html","UTF-8");
        } else {
            reportBinding.permissions.loadData(getString(R.string.no_permissions),"text/plain", "UTF-8");
        }

        reportBinding.analysed.setVisibility(View.GONE);
        reportBinding.trackersTitle.setVisibility(View.VISIBLE);
        reportBinding.trackers.setVisibility(View.VISIBLE);
        //get trackers
        Report report = DatabaseManager.getInstance(context).getReportFor(packageName,versionName);
        Set<Tracker> trackers = null;
        if(report != null) {
            trackers = DatabaseManager.getInstance(context).getTrackers(report.trackers);
        } else {
            reportBinding.analysed.setVisibility(View.VISIBLE);
            reportBinding.trackersTitle.setVisibility(View.GONE);
            reportBinding.trackers.setVisibility(View.GONE);
        }
        //setup trackers report
        String trackers_text;
        if(trackers != null && trackers.size() > 0)
            trackers_text = context.getString(R.string.trackers)+" "+String.valueOf(trackers.size());
        else
            trackers_text = context.getString(R.string.trackers);
        reportBinding.trackersTitle.setText(trackers_text);

        //setup trackers lists
        //build html tracker list
        if(trackers != null && trackers.size() > 0) {
            List<String> trackersName = new ArrayList<>();
            for (Tracker tracker : trackers) {
                trackersName.add(tracker.name);
            }
            String html = buildHtmlList(trackersName);
            reportBinding.trackers.loadData(html,"text/html","UTF-8");
        } else {
            reportBinding.trackers.loadData(getString(R.string.no_trackers),"text/plain","UTF-8");
        }

        //setup creator
        if(report != null)
            reportBinding.creator.setText(DatabaseManager.getInstance(context).getCreator(report.appId));
        else
            reportBinding.creator.setVisibility(View.GONE);

        //setup installed
        String installed_str = context.getString(R.string.installed) +" "+ versionName;
        reportBinding.installedVersion.setText(installed_str);

        //setup reportversion
        reportBinding.reportVersion.setVisibility(View.VISIBLE);
        if(report != null && !report.version.equals(versionName)) {
            String report_str = context.getString(R.string.report_version)+" "+report.version;
            reportBinding.reportVersion.setText(report_str);
        }
        else
            reportBinding.reportVersion.setVisibility(View.GONE);

        //setup report url
        if(report != null)
            reportBinding.reportUrl.setText("https://reports.exodus-privacy.eu.org/reports/"+report.id+"/");
    }

    private String buildHtmlList(List<String> list) {
        String html = "<ul>";
        for (String item : list) {
            html += "<li>"+item+"</li>";
        }
        html += "</li>";
        return html;
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }
}
