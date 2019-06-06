package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import org.eu.exodus_privacy.exodusprivacy.Utils;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class ComputeAppListTask extends AsyncTask<Void, Void, List<ApplicationViewModel>> {

    interface Listener {
        void onAppsComputed(List<ApplicationViewModel> apps);
    }

    private static final String gStore = "com.android.vending";

    private WeakReference<PackageManager> packageManagerRef;
    private WeakReference<DatabaseManager> databaseManagerRef;
    private WeakReference<Listener> listenerRef;

    ComputeAppListTask(WeakReference<PackageManager> packageManagerRef,
                       WeakReference<DatabaseManager> databaseManagerRef,
                       WeakReference<Listener> listenerRef) {
        this.packageManagerRef = packageManagerRef;
        this.databaseManagerRef = databaseManagerRef;
        this.listenerRef = listenerRef;
    }

    protected List<ApplicationViewModel> doInBackground(Void... params) {
        PackageManager packageManager = packageManagerRef.get();
        DatabaseManager databaseManager = databaseManagerRef.get();

        List<ApplicationViewModel> vms = new ArrayList<>();
        if(packageManager != null && databaseManager != null) {
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            applyStoreFilter(installedPackages, databaseManager, packageManager);
            vms = convertPackagesToViewModels(installedPackages, databaseManager, packageManager);
        }
        return vms;
    }

    @Override
    protected void onPostExecute(List<ApplicationViewModel> vms) {
        Listener listener = listenerRef.get();

        if(listener != null) {
            listener.onAppsComputed(vms);
        }
    }

    private List<ApplicationViewModel> convertPackagesToViewModels(List<PackageInfo> infos,
                                                                   DatabaseManager databaseManager,
                                                                   PackageManager packageManager) {
        ArrayList<ApplicationViewModel> appsToBuild = new ArrayList<>(infos.size());
        for (PackageInfo pi : infos) {
            appsToBuild.add(buildViewModelFromPackageInfo(pi, databaseManager, packageManager));
        }
        return appsToBuild;
    }

    private ApplicationViewModel buildViewModelFromPackageInfo(PackageInfo pi,
                                                               DatabaseManager databaseManager,
                                                               PackageManager packageManager) {
        ApplicationViewModel vm = new ApplicationViewModel();

        vm.versionName = pi.versionName;
        vm.packageName = pi.packageName;
        vm.versionCode = pi.versionCode;
        vm.requestedPermissions = pi.requestedPermissions;

        if (vm.versionName != null)
            vm.report = databaseManager.getReportFor(vm.packageName, vm.versionName);
        else {
            vm.report = databaseManager.getReportFor(vm.packageName, vm.versionCode);
        }

        if (vm.report != null) {
            vm.trackers = databaseManager.getTrackers(vm.report.trackers);
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

    private void applyStoreFilter(List<PackageInfo> packageInfos,
                                  DatabaseManager databaseManager,
                                  PackageManager packageManager) {
        List<PackageInfo> toRemove = new ArrayList<>();
        for (PackageInfo packageInfo : packageInfos) {
            String packageName = packageInfo.packageName;
            String installerPackageName = packageManager.getInstallerPackageName(packageName);
            if (!gStore.equals(installerPackageName)) {
                String auid = Utils.getCertificateSHA1Fingerprint(packageManager, packageName);
                String appuid = databaseManager.getAUID(packageName);
                if(!auid.equalsIgnoreCase(appuid)) {
                    toRemove.add(packageInfo);
                }
            }

            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName,0);
                if(!appInfo.enabled) {
                    toRemove.add(packageInfo);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        packageInfos.removeAll(toRemove);
    }

}
