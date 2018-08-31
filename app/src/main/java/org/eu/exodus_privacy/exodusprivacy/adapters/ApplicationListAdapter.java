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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.Utils;
import org.eu.exodus_privacy.exodusprivacy.databinding.AppItemBinding;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ApplicationListAdapter extends RecyclerView.Adapter {

    private List<PackageInfo> packages;
    private PackageManager packageManager;
    private OnAppClickListener onAppClickListener;
    private static final String gStore = "com.android.vending";
    private String filter = "";
    private final int HIDDEN_APP = 0;
    private final int DISPLAYED_APP = 1;
    private Context context;


    private Comparator<PackageInfo> alphaPackageComparator = new Comparator<PackageInfo>() {
        @Override
        public int compare(PackageInfo pack1, PackageInfo pack2) {
            String pkg1 = packageManager.getApplicationLabel(pack1.applicationInfo).toString();
            String pkg2 = packageManager.getApplicationLabel(pack2.applicationInfo).toString();
            return pkg1.compareToIgnoreCase(pkg2);
        }
    };

    public ApplicationListAdapter(Context context, PackageManager manager, OnAppClickListener listener) {
        onAppClickListener = listener;
        this.context = context;
        setPackageManager(manager);
    }

    private void setInstalledPackages(List<PackageInfo> installedPackages) {
        packages = installedPackages;
        applyStoreFilter();
        Collections.sort(packages, alphaPackageComparator);
        notifyDataSetChanged();
    }

    private void applyStoreFilter() {
        List<PackageInfo> toRemove = new ArrayList<>();
        for (PackageInfo pkg : packages) {
            if (!gStore.equals(packageManager.getInstallerPackageName(pkg.packageName))) {

                String auid = Utils.getCertificateSHA1Fingerprint(packageManager,pkg.packageName);
                String appuid = DatabaseManager.getInstance(context).getAUID(pkg.packageName);
                if(!auid.equalsIgnoreCase(appuid)) {
                    toRemove.add(pkg);
                }
            }

            try {
                ApplicationInfo info = packageManager.getApplicationInfo(pkg.packageName,0);
                if(!info.enabled) {
                    toRemove.add(pkg);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        packages.removeAll(toRemove);
    }

    @Override
    public int getItemViewType(int position){
        return (Pattern.compile(Pattern.quote(filter.trim()), Pattern.CASE_INSENSITIVE).matcher(packageManager.getApplicationLabel(packages.get(position).applicationInfo)).find())?DISPLAYED_APP:HIDDEN_APP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if( viewType == HIDDEN_APP)
            return new ApplicationEmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_empty, parent, false));
        else
            return new ApplicationListViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.app_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if( viewHolder.getItemViewType() == DISPLAYED_APP) {
            final ApplicationListViewHolder holder = (ApplicationListViewHolder) viewHolder;
            holder.setData(packages.get(position));
            //noinspection Convert2Lambda
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAppClickListener.onAppClick(holder.packageInfo);
                }
            });
        }else {
            //noinspection unused
            final ApplicationEmptyViewHolder holder = (ApplicationEmptyViewHolder) viewHolder;
            //If something should be done for app that are hidden, it's here
        }
    }


    @Override
    public int getItemCount() {
        return packages.size();
    }

    public void setPackageManager(PackageManager manager) {
        packageManager = manager;
        if(packageManager != null) {
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            setInstalledPackages(installedPackages);
        }
    }

    class ApplicationEmptyViewHolder extends RecyclerView.ViewHolder{
        ApplicationEmptyViewHolder(View itemView) {
            super(itemView);
        }
    }


    class ApplicationListViewHolder extends RecyclerView.ViewHolder {

        PackageInfo packageInfo;
        AppItemBinding appItemBinding;

        ApplicationListViewHolder(AppItemBinding binding) {
            super(binding.getRoot());
            appItemBinding = binding;
        }

        public void setData(PackageInfo data) {
            packageInfo = data;

            Context context = appItemBinding.getRoot().getContext();

            //reinit view state
            appItemBinding.otherVersion.setVisibility(View.GONE);
            appItemBinding.analysed.setVisibility(View.GONE);
            appItemBinding.appTrackerNb.setVisibility(View.VISIBLE);


            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;

            //get logo
            try {
                appItemBinding.appLogo.setImageDrawable(packageManager.getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //get name
            appItemBinding.appName.setText(packageManager.getApplicationLabel(packageInfo.applicationInfo));

            //get permissions
            if(packageInfo.requestedPermissions != null) {
                appItemBinding.appPermissionNb.setText(context.getString(R.string.permissions) + " " + String.valueOf(data.requestedPermissions.length));
            } else {
                appItemBinding.appPermissionNb.setText(context.getString(R.string.permissions) + " " + String.valueOf(0));
            }
            //get reports
            Report report = DatabaseManager.getInstance(context).getReportFor(packageName, versionName);
            if(report != null) {
                Set<Tracker> trackers = DatabaseManager.getInstance(context).getTrackers(report.trackers);
                appItemBinding.appTrackerNb.setText(context.getString(R.string.trackers) + " " + trackers.size());
                if(!report.version.equals(data.versionName)) {
                    appItemBinding.otherVersion.setVisibility(View.VISIBLE);
                }

            } else {
                appItemBinding.appTrackerNb.setVisibility(View.GONE);
                appItemBinding.analysed.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnAppClickListener {
        void onAppClick(PackageInfo packageInfo);
    }


    public void filter(String text) {
        filter = text;
        notifyDataSetChanged();
    }
}
