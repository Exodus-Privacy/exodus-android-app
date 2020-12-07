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

package org.eu.exodus_privacy.exodusprivacy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.AppItemBinding;
import org.eu.exodus_privacy.exodusprivacy.fragments.AppListFragment;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ApplicationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final OnAppClickListener onAppClickListener;
    private final int HIDDEN_APP = 0;
    private final int DISPLAYED_APP = 1;
    private final Comparator<ApplicationViewModel> alphaPackageComparator = (app1, app2) -> {
        if (app1.label != null && app2.label != null)
            return app1.label.toString().compareToIgnoreCase(app2.label.toString());
        else if (app2.label != null)
            return -1;
        else if (app1.label != null)
            return 1;
        else
            return 0;
    };
    private List<ApplicationViewModel> applicationViewModels;
    private Object filter = "";
    private AppListFragment.Type filterType = AppListFragment.Type.NAME;
    private int displayedApp = 0;

    public ApplicationListAdapter(OnAppClickListener listener) {
        applicationViewModels = new ArrayList<>();
        onAppClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return applicationViewModels.get(position).isVisible ? DISPLAYED_APP : HIDDEN_APP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HIDDEN_APP)
            return new ApplicationEmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_empty, parent, false));
        else
            return new ApplicationListViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.app_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == DISPLAYED_APP) {
            final ApplicationListViewHolder holder = (ApplicationListViewHolder) viewHolder;
            ApplicationViewModel vm = applicationViewModels.get(position);
            holder.setViewModel(vm);
            //noinspection Convert2Lambda
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAppClickListener.onAppClick(vm, position);
                }
            });
        } else //noinspection RedundantSuppression
        {
            //noinspection unused
            final ApplicationEmptyViewHolder holder = (ApplicationEmptyViewHolder) viewHolder;
            //If something should be done for app that are hidden, it's here
        }
    }

    @Override
    public int getItemCount() {
        return applicationViewModels.size();
    }

    public void displayAppList(List<ApplicationViewModel> applications) {
        applicationViewModels = applications;
        Collections.sort(applicationViewModels, alphaPackageComparator);
        filter(filterType, filter);
    }

    public int getDisplayedApps() {
        return displayedApp;
    }

    public void filter(AppListFragment.Type type, Object filterObject) {
        displayedApp = 0;
        if (type.equals(AppListFragment.Type.NAME)) {
            filter = filterObject;
            filterType = type;
            String filterStr = (String) filterObject;

            Pattern p = Pattern.compile(Pattern.quote(filterStr.trim()), Pattern.CASE_INSENSITIVE);
            for (ApplicationViewModel app : applicationViewModels) {
                app.isVisible = p.matcher(app.label).find();
                if (app.isVisible)
                    displayedApp++;
            }
        } else if (type.equals(AppListFragment.Type.TRACKER)) {
            filter = filterObject;
            filterType = type;
            Long filterLng = (Long) filterObject;

            for (ApplicationViewModel app : applicationViewModels) {
                app.isVisible = false;
                if (app.trackers != null) {
                    for (Tracker tracker : app.trackers) {
                        if (tracker.id == filterLng) {
                            app.isVisible = true;
                            displayedApp++;
                            break;
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface OnAppClickListener {
        void onAppClick(ApplicationViewModel vm, int position);
    }

    static class ApplicationEmptyViewHolder extends RecyclerView.ViewHolder {
        ApplicationEmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ApplicationListViewHolder extends RecyclerView.ViewHolder {

        ApplicationViewModel viewModel;
        AppItemBinding appItemBinding;

        ApplicationListViewHolder(AppItemBinding binding) {
            super(binding.getRoot());
            appItemBinding = binding;
        }

        void setViewModel(ApplicationViewModel vm) {
            viewModel = vm;

            Context context = appItemBinding.getRoot().getContext();

            //reinit view state
            appItemBinding.otherVersion.setVisibility(View.GONE);
            appItemBinding.analysed.setVisibility(View.GONE);
            appItemBinding.appTrackerNb.setVisibility(View.VISIBLE);
            appItemBinding.appTracker.setVisibility(View.VISIBLE);

            String versionName = viewModel.versionName;
            long versionCode = viewModel.versionCode;

            appItemBinding.appLogo.setImageDrawable(viewModel.icon);

            appItemBinding.appName.setText(viewModel.label);
            appItemBinding.source.setText(context.getString(R.string.source, viewModel.source));

            long size = viewModel.requestedPermissions != null ? viewModel.requestedPermissions.length : 0;
            appItemBinding.appPermissionNb.setText(String.valueOf(size));
            if (size == 0)
                appItemBinding.appPermissionNb.setBackgroundResource(R.drawable.square_green);
            else if (size < 5)
                appItemBinding.appPermissionNb.setBackgroundResource(R.drawable.square_light_yellow);
            else
                appItemBinding.appPermissionNb.setBackgroundResource(R.drawable.square_light_red);

            Report report = viewModel.report;
            if (report != null) {
                Set<Tracker> trackers = viewModel.trackers;

                size = trackers.size();
                appItemBinding.appTrackerNb.setText(String.valueOf(size));
                if (size == 0)
                    appItemBinding.appTrackerNb.setBackgroundResource(R.drawable.square_green);
                else if (size < 5)
                    appItemBinding.appTrackerNb.setBackgroundResource(R.drawable.square_light_yellow);
                else
                    appItemBinding.appTrackerNb.setBackgroundResource(R.drawable.square_light_red);

                if (versionName != null && !report.version.equals(viewModel.versionName)) {
                    String string = context.getString(R.string.tested, versionName, report.version);
                    appItemBinding.otherVersion.setText(string);
                    appItemBinding.otherVersion.setVisibility(View.VISIBLE);
                } else if (versionName == null && report.versionCode != versionCode) {
                    String string = context.getString(R.string.tested, String.valueOf(versionCode), String.valueOf(report.versionCode));
                    appItemBinding.otherVersion.setText(string);
                    appItemBinding.otherVersion.setVisibility(View.VISIBLE);
                }
            } else {
                appItemBinding.appTrackerNb.setVisibility(View.GONE);
                appItemBinding.appTracker.setVisibility(View.GONE);
                appItemBinding.analysed.setVisibility(View.VISIBLE);
            }
        }
    }
}
