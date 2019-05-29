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
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private List<ApplicationViewModel> applicationViewModels;
    private PackageManager packageManager;
    private OnAppClickListener onAppClickListener;
    private static final String gStore = "com.android.vending";
    private String filter = "";
    private final int HIDDEN_APP = 0;
    private final int DISPLAYED_APP = 1;
    private Context context;


    private Comparator<ApplicationViewModel> alphaPackageComparator = new Comparator<ApplicationViewModel>() {
        @Override
        public int compare(ApplicationViewModel app1, ApplicationViewModel app2) {
            return app1.label.toString().compareToIgnoreCase(app2.label.toString());
        }
    };

    public ApplicationListAdapter(Context context, PackageManager manager, OnAppClickListener listener) {
        applicationViewModels = new ArrayList<>();
        onAppClickListener = listener;
        this.context = context;
        setPackageManager(manager);
    }

    private void setInstalledPackages(List<PackageInfo> installedPackages) {
        List<ApplicationViewModel> viewModels = convertPackagesToViewModels(installedPackages);
        applyStoreFilter(viewModels);
        Collections.sort(viewModels, alphaPackageComparator);
        applicationViewModels = viewModels;
        notifyDataSetChanged();
    }

    private void applyStoreFilter(List<ApplicationViewModel> apps) {
        List<ApplicationViewModel> toRemove = new ArrayList<>();
        for (ApplicationViewModel app : apps) {
            if (!gStore.equals(app.installerPackageName)) {

                String auid = Utils.getCertificateSHA1Fingerprint(packageManager,app.packageName);
                String appuid = DatabaseManager.getInstance(context).getAUID(app.packageName);
                if(!auid.equalsIgnoreCase(appuid)) {
                    toRemove.add(app);
                }
            }

            try {
                ApplicationInfo info = packageManager.getApplicationInfo(app.packageName,0);
                if(!info.enabled) {
                    toRemove.add(app);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        apps.removeAll(toRemove);
    }

    private List<ApplicationViewModel> convertPackagesToViewModels(List<PackageInfo> infos) {
        ArrayList<ApplicationViewModel> appsToBuild = new ArrayList<>(infos.size());
        for (PackageInfo pi : infos) {
            appsToBuild.add(buildViewModelFromPackageInfo(pi));
        }
        return appsToBuild;
    }

    private ApplicationViewModel buildViewModelFromPackageInfo(PackageInfo pi) {
        ApplicationViewModel vm = new ApplicationViewModel();

        vm.versionName = pi.versionName;
        vm.packageName = pi.packageName;
        vm.versionCode = pi.versionCode;
        vm.requestedPermissions = pi.requestedPermissions;

        DatabaseManager dm = DatabaseManager.getInstance(context);
        if(vm.versionName != null)
            vm.report = dm.getReportFor(vm.packageName, vm.versionName);
        else {
            vm.report = dm.getReportFor(vm.packageName, vm.versionCode);
        }

        if(vm.report != null) {
            vm.trackers = dm.getTrackers(vm.report.trackers);
        }

        try {
            vm.icon = packageManager.getApplicationIcon(vm.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        vm.label = packageManager.getApplicationLabel(pi.applicationInfo);
        vm.installerPackageName = packageManager.getInstallerPackageName(vm.packageName);
        vm.isVisible = true;

        return vm;
    }

    @Override
    public int getItemViewType(int position){
        return applicationViewModels.get(position).isVisible ? DISPLAYED_APP : HIDDEN_APP;
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
            holder.setViewModel(applicationViewModels.get(position));
            //noinspection Convert2Lambda
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PackageInfo packageInfo = packageManager.getPackageInfo(holder.viewModel.packageName, PackageManager.GET_PERMISSIONS);
                        onAppClickListener.onAppClick(packageInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
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
        return applicationViewModels.size();
    }

    public void setPackageManager(PackageManager manager) {
        packageManager = manager;
        if(packageManager != null) {
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            setInstalledPackages(installedPackages);
        }
    }

    /**
     * This class holds the data needed to display an application cell in the RecyclerView
     */
    public class ApplicationViewModel {
        public String packageName;
        public String versionName;
        public int versionCode;
        public String[] requestedPermissions;
        public @Nullable Report report;
        public Set<Tracker> trackers;
        public @Nullable Drawable icon;
        public CharSequence label;
        public String installerPackageName;
        public boolean isVisible;
    }

    class ApplicationEmptyViewHolder extends RecyclerView.ViewHolder{
        ApplicationEmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ApplicationListViewHolder extends RecyclerView.ViewHolder {

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

            long size = viewModel.requestedPermissions != null ? viewModel.requestedPermissions.length : 0;
            appItemBinding.appPermissionNb.setText(String.valueOf(size));
            if(size == 0)
                appItemBinding.appPermissionNb.setBackgroundResource(R.drawable.square_green);
            else if (size < 5)
                appItemBinding.appPermissionNb.setBackgroundResource(R.drawable.square_yellow);
            else
                appItemBinding.appPermissionNb.setBackgroundResource(R.drawable.square_red);

            Report report = viewModel.report;
            if(report != null) {
                Set<Tracker> trackers = viewModel.trackers;

                size = trackers.size();
                appItemBinding.appTrackerNb.setText(String.valueOf(size));
                if(size == 0)
                    appItemBinding.appTrackerNb.setBackgroundResource(R.drawable.square_green);
                else if (size < 5)
                    appItemBinding.appTrackerNb.setBackgroundResource(R.drawable.square_yellow);
                else
                    appItemBinding.appTrackerNb.setBackgroundResource(R.drawable.square_red);

                if(versionName != null && !report.version.equals(viewModel.versionName)) {
                    String string = context.getString(R.string.tested,versionName, report.version);
                    appItemBinding.otherVersion.setText(string);
                    appItemBinding.otherVersion.setVisibility(View.VISIBLE);
                } else if (versionName == null && report.versionCode != versionCode) {
                    String string = context.getString(R.string.tested,String.valueOf(versionCode),String.valueOf(report.versionCode));
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

    public interface OnAppClickListener {
        void onAppClick(PackageInfo packageInfo);
    }


    public void filter(String text) {
        filter = text;

        Pattern p = Pattern.compile(Pattern.quote(filter.trim()), Pattern.CASE_INSENSITIVE);
        for (ApplicationViewModel app : applicationViewModels) {
            app.isVisible = p.matcher(app.label).find();
        }

        notifyDataSetChanged();
    }
}
