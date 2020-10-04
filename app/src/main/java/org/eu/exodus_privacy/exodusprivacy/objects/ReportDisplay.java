package org.eu.exodus_privacy.exodusprivacy.objects;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReportDisplay {
    public Report report;
    public String packageName;
    public String versionName;
    public String displayName;
    public String creator;
    public long versionCode;
    public Drawable logo;
    public List<Permission> permissions;
    public Set<Tracker> trackers;
    public String source;
    public String viewOnStore;


    private ReportDisplay(){

    }

    public static ReportDisplay buildReportDisplay(Context context, ApplicationViewModel model, PackageManager manager, PackageInfo info) {
        ReportDisplay reportDisplay = new ReportDisplay();
        reportDisplay.packageName = model.packageName;
        reportDisplay.versionName = model.versionName;
        reportDisplay.versionCode = model.versionCode;
        reportDisplay.displayName = model.label.toString();

        reportDisplay.report = model.report;
        reportDisplay.source = context.getString(R.string.source,model.source);
        reportDisplay.viewOnStore = context.getString(model.source.equals("google")? R.string.view_on_google_play : R.string.view_on_fdroid);

        reportDisplay.trackers = model.trackers;

        if (reportDisplay.report != null)
            reportDisplay.creator =  DatabaseManager.getInstance(context).getCreator(reportDisplay.report.appId);

        List<Permission> requestedPermissions= new ArrayList<>();
        if (info.requestedPermissions != null) {
            for(int i = 0; i < info.requestedPermissions.length; i++) {
                Permission permission = new Permission();
                permission.fullName = info.requestedPermissions[i];
                try {
                    PermissionInfo permissionInfo = manager.getPermissionInfo(permission.fullName,PackageManager.GET_META_DATA);
                    if(permissionInfo.loadDescription(manager) != null)
                        permission.description = permissionInfo.loadDescription(manager).toString();
                    if(permissionInfo.loadLabel(manager) != null)
                        permission.name = permissionInfo.loadLabel(manager).toString();
                    permission.dangerous = permissionInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS;
                    if(permission.fullName.equals(Manifest.permission.WRITE_SETTINGS) || permission.fullName.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) //Special permissions
                        permission.dangerous = true;

                    if (permissionInfo.group != null) {
                        PermissionGroupInfo permissionGroupInfo = manager.getPermissionGroupInfo(permissionInfo.group, PackageManager.GET_META_DATA);
                        if(permissionGroupInfo.loadIcon(manager) != null)
                            permission.icon = permissionGroupInfo.loadIcon(manager);
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.getLocalizedMessage();
                }
                requestedPermissions.add(permission);
            }
        }
        reportDisplay.permissions = requestedPermissions;

        reportDisplay.logo = model.icon;

        return reportDisplay;
    }
}
