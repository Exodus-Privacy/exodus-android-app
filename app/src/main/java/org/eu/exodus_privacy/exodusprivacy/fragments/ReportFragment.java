
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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.ReportViewModel;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.adapters.PermissionListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.TrackerListAdapter;
import org.eu.exodus_privacy.exodusprivacy.databinding.ReportBinding;
import org.eu.exodus_privacy.exodusprivacy.objects.ReportDisplay;
public class ReportFragment  extends Fragment implements Updatable {

    private PackageManager packageManager;
    private PackageInfo packageInfo = null;
    private ReportBinding reportBinding;
    private TrackerListAdapter.OnTrackerClickListener trackerClickListener;
    private ApplicationViewModel model;

    public static ReportFragment newInstance(PackageManager packageManager,ApplicationViewModel model, PackageInfo packageInfo, TrackerListAdapter.OnTrackerClickListener trackerClickListener) {
        ReportFragment fragment = new ReportFragment();
        fragment.setPackageManager(packageManager);
        fragment.setPackageInfo(packageInfo);
        fragment.setApplicationViewModel(model);
        fragment.setOnTrackerClickListener(trackerClickListener);
        return fragment;
    }

    private void setApplicationViewModel(ApplicationViewModel model) {
        this.model = model;
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
        onUpdateComplete();
        return reportBinding.getRoot();
    }

    @Override
    public void onUpdateComplete() {
        if(model != null)
            onUpdateComplete(model);
    }

    public void onUpdateComplete(ApplicationViewModel model) {
        Context context = reportBinding.getRoot().getContext();

        ReportDisplay reportDisplay = ReportDisplay.buildReportDisplay(context,model,packageManager,packageInfo);
        ReportViewModel viewModel = new ReportViewModel();
        viewModel.setReportDisplay(reportDisplay);
        reportBinding.setReportInfo(viewModel);


        reportBinding.permissions.setLayoutManager(new LinearLayoutManager(context));
        PermissionListAdapter permissionAdapter = new PermissionListAdapter(reportDisplay.permissions);
        reportBinding.permissions.setNestedScrollingEnabled(false);
        reportBinding.permissions.setAdapter(permissionAdapter);


        //setup trackers lists
        reportBinding.trackers.setLayoutManager(new LinearLayoutManager(context));
        TrackerListAdapter trackerAdapter = new TrackerListAdapter(reportDisplay.trackers,R.layout.tracker_item, trackerClickListener);
        reportBinding.trackers.setNestedScrollingEnabled(false);
        reportBinding.trackers.setAdapter(trackerAdapter);

        reportBinding.reportDate.setText(viewModel.getReportDate(context));
        reportBinding.creatorValue.setText(viewModel.getCreator(context));
        reportBinding.codeSignature.setText(viewModel.getCodeSignatureInfo(context));
        reportBinding.codePermission.setText(viewModel.getCodePermissionInfo(context));

        reportBinding.trackerExplanation.setText(getText(R.string.tracker_infos));
        reportBinding.trackerExplanation.setMovementMethod(LinkMovementMethod.getInstance());
        reportBinding.trackerExplanation.setClickable(true);

        reportBinding.permissionExplanationDangerous.setText(getText(R.string.permission_infos_dangerous));
        reportBinding.permissionExplanationDangerous.setMovementMethod(LinkMovementMethod.getInstance());
        reportBinding.permissionExplanationDangerous.setClickable(true);

        reportBinding.permissionExplanation.setText(getText(R.string.permission_infos));
        reportBinding.permissionExplanation.setMovementMethod(LinkMovementMethod.getInstance());
        reportBinding.permissionExplanation.setClickable(true);

        reportBinding.viewStore.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if(reportDisplay.source.contains("google"))
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+reportDisplay.packageName));
            else
                intent.setData(Uri.parse("https://f-droid.org/packages/"+reportDisplay.packageName));
            startActivity(intent);
        });

        if(reportDisplay.report != null) {
            reportBinding.reportUrl.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://reports.exodus-privacy.eu.org/reports/" + reportDisplay.report.id + "/"));
                startActivity(intent);
            });
        }
    }

    private void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    private void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    private void setOnTrackerClickListener(TrackerListAdapter.OnTrackerClickListener listener) {
        trackerClickListener = listener;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_filter);
        item.setVisible(false);
    }

    public ApplicationViewModel getModel() {
        return model;
    }
}
