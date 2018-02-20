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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApplicationListAdapter extends android.support.v7.widget.RecyclerView.Adapter<ApplicationListAdapter.ApplicationListViewHolder>{

    private List<PackageInfo> packages;
    private PackageManager packageManager;
    private OnAppClickListener onAppClickListener;
    private final String gStore = "com.android.vending";

    private Comparator<PackageInfo> alphaPackageComparator = new Comparator<PackageInfo>() {
        @Override
        public int compare(PackageInfo pack1, PackageInfo pack2) {
            String pkg1 = packageManager.getApplicationLabel(pack1.applicationInfo).toString();
            String pkg2 = packageManager.getApplicationLabel(pack2.applicationInfo).toString();
            return pkg1.compareToIgnoreCase(pkg2);
        }
    };

    public ApplicationListAdapter(List<PackageInfo> installedPackages, PackageManager manager, OnAppClickListener listener) {
        packageManager = manager;
        onAppClickListener = listener;
        setInstalledPackages(installedPackages);
    }

    private void setInstalledPackages(List<PackageInfo> installedPackages) {
        packages = installedPackages;
        applyStoreFilter();
        Collections.sort(packages,alphaPackageComparator);
        notifyDataSetChanged();
    }

    private void applyStoreFilter() {
        List<PackageInfo> toRemove = new ArrayList<>();
        for(PackageInfo pkg : packages) {
            if (!gStore.equals(packageManager.getInstallerPackageName(pkg.packageName))) {
                toRemove.add(pkg);
            }
        }
        packages.removeAll(toRemove);
    }

    @Override
    public ApplicationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item,parent,false);
        return new ApplicationListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ApplicationListViewHolder holder, int position) {
        holder.setData(packages.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAppClickListener.onAppClick(holder.packageInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public void setPackageManager(PackageManager manager) {
        packageManager = manager;
    }

    class ApplicationListViewHolder extends RecyclerView.ViewHolder {

        ImageView app_logo;
        TextView app_name;
        TextView app_permission_nb;
        TextView app_tracker_nb;
        TextView tested;
        TextView analysed;
        PackageInfo packageInfo;

        ApplicationListViewHolder(View itemView) {
            super(itemView);
            app_logo = itemView.findViewById(R.id.app_logo);
            app_name = itemView.findViewById(R.id.app_name);
            app_permission_nb = itemView.findViewById(R.id.app_permission_nb);
            app_tracker_nb = itemView.findViewById(R.id.app_tracker_nb);
            tested = itemView.findViewById(R.id.other_version);
            analysed = itemView.findViewById(R.id.analysed);
        }

        public void setData(PackageInfo data) {
            packageInfo = data;

            //reinit view state
            tested.setVisibility(View.GONE);
            analysed.setVisibility(View.GONE);
            app_tracker_nb.setVisibility(View.VISIBLE);


            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;

            //get logo
            try {
                app_logo.setImageDrawable(packageManager.getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //get name
            app_name.setText(packageManager.getApplicationLabel(packageInfo.applicationInfo));

            //get permissions
            if(packageInfo.requestedPermissions != null) {
                app_permission_nb.setText(app_name.getContext().getString(R.string.permissions) + " " + String.valueOf(data.requestedPermissions.length));
            } else {
                app_permission_nb.setText(app_name.getContext().getString(R.string.permissions) + " " + String.valueOf(0));
            }
            //get reports
            Report report = DatabaseManager.getInstance(app_logo.getContext()).getReportFor(packageName, versionName);
            if(report != null) {
                Set<Tracker> trackers = DatabaseManager.getInstance(app_logo.getContext()).getTrackers(report.trackers);
                app_tracker_nb.setText(app_name.getContext().getString(R.string.trackers) + " " + trackers.size());
                if(!report.version.equals(data.versionName)) {
                    tested.setVisibility(View.VISIBLE);
                }

            } else {
                app_tracker_nb.setVisibility(View.GONE);
                analysed.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnAppClickListener {
        void onAppClick(PackageInfo packageInfo);
    }
}
