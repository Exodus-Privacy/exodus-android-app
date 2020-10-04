package org.eu.exodus_privacy.exodusprivacy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.eu.exodus_privacy.exodusprivacy.objects.Permission;
import org.eu.exodus_privacy.exodusprivacy.objects.ReportDisplay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class ReportViewModel extends BaseObservable {
    private ReportDisplay reportDisplay;

    public void setReportDisplay(ReportDisplay report){
        this.reportDisplay = report;
        notifyChange();
    }

    @Bindable
    public String getName() {
        return reportDisplay.displayName;
    }

    @Bindable
    public Drawable getLogo() {
        return reportDisplay.logo;
    }


    public int getPermissionNumber() {
        return reportDisplay.permissions != null ? reportDisplay.permissions.size() : 0;
    }

    @Bindable
    public String getPermissionNumberStr() {
        return String.valueOf(getPermissionNumber());
    }

    @Bindable
    public int getPermissionColor() {
        return getColor(getPermissionNumber());
    }

    @Bindable
    public boolean getPermissionVisibility() {
        return reportDisplay.permissions != null;
    }

    @Bindable
    public boolean getHasPermissionDangerous() {
        for(Permission perm : reportDisplay.permissions) {
            if(perm.dangerous)
                return true;
        }
        return false;
    }

    public int getTrackerNumber() {
        return reportDisplay.trackers != null ? reportDisplay.trackers.size() : 0;
    }

    @Bindable
    public String getTrackerNumberStr() {
        return String.valueOf(getTrackerNumber());
    }


    @Bindable
    public boolean getTrackerVisibility() {
        return reportDisplay.trackers != null;
    }

    @Bindable
    public int getTrackerColor() {
        return getColor(getTrackerNumber());
    }

    public String getCreator(Context context) {
        String creator = reportDisplay.creator != null ? reportDisplay.creator : "";
        if (reportDisplay.report != null && !reportDisplay.report.downloads.isEmpty()) {
            String download = reportDisplay.report.downloads;
            download = download.replace("downloads",context.getString(R.string.downloads));
            creator += " (" + download + ")";
        }
        return creator;
    }

    @Bindable
    public boolean getCreatorVisibility() {
        return reportDisplay.creator != null && !reportDisplay.creator.isEmpty();
    }

    @Bindable
    public String getInstalledVersion() {
        return reportDisplay.versionName != null ? reportDisplay.versionName : String.valueOf(reportDisplay.versionCode);
    }

    @Bindable
    public String getReportVersion() {
        if(reportDisplay.report != null) {
            if (reportDisplay.versionName != null && !reportDisplay.report.version.equals(reportDisplay.versionName)) {
                return reportDisplay.report.version;
            } else if (reportDisplay.versionName == null && reportDisplay.report.versionCode != reportDisplay.versionCode) {
                return String.valueOf(reportDisplay.report.versionCode);
            }
        }
        return "";
    }

    @Bindable
    public boolean getReportVersionVisibility() {
        return !getReportVersion().isEmpty();
    }

    @Bindable
    public boolean getReportVisibility() {
        return reportDisplay.report != null;
    }

    public String getReportDate(Context context) {
        String reportDate = "";
        if(reportDisplay.report == null)
            return reportDate;


        DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
        reportDate = context.getString(R.string.created_date)+" "+dateFormat.format(reportDisplay.report.creationDate.getTime());
        if (reportDisplay.report.creationDate.getTime().compareTo(reportDisplay.report.updateDate.getTime())!=0)
            reportDate += " "+context.getString(R.string.and_updated)+" "+dateFormat.format(reportDisplay.report.updateDate.getTime())+".";
        return reportDate;
    }

    public String getCodeSignatureInfo(Context context) {
       if(reportDisplay.trackers != null && reportDisplay.trackers.size() > 0)
           return context.getString(R.string.code_signature_found);
       else if(reportDisplay.trackers != null)
           return context.getString(R.string.code_signature_not_found);
       else
           return "";
    }

    public String getCodePermissionInfo(Context context) {
        if(reportDisplay.permissions != null && reportDisplay.permissions.size() > 0)
            return context.getString(R.string.code_permission_found);
        else if(reportDisplay.permissions != null)
            return context.getString(R.string.code_permission_not_found);
        else
            return "";
    }


    private int getColor(int number) {
        if (number == 0)
            return R.drawable.square_green;
        else if(number < 5)
            return R.drawable.square_light_yellow;
        else
            return R.drawable.square_light_red;
    }

    @Bindable
    public String getSource() {
        return reportDisplay.source;
    }

    public String getViewOnStore() {
        return reportDisplay.viewOnStore;
    }



}
