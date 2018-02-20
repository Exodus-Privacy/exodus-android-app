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

package org.eu.exodus_privacy.exodusprivacy.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.eu.exodus_privacy.exodusprivacy.objects.Application;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseManager extends SQLiteOpenHelper {

    private static DatabaseManager instance;

    private DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    public static DatabaseManager getInstance(Context context) {
        if(instance == null)
            instance = new DatabaseManager(context,"Exodus.db",null,1);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table if not exists applications (id INTEGER primary key autoincrement, package TEXT, name TEXT, creator TEXT);");
        db.execSQL("Create Table if not exists reports (id INTEGER primary key, creation INTEGER, updateat INTEGER, downloads TEXT, version TEXT, version_code INTEGER, app_id INTEGER, foreign key(app_id) references applications(id));");
        db.execSQL("Create Table if not exists trackers (id INTEGER primary key, name TEXT, creation_date INTEGER, code_signature TEXT, network_signature TEXT, website TEXT, description TEXT);");

        db.execSQL("Create Table if not exists trackers_reports (id INTEGER primary key autoincrement, tracker_id INTEGER, report_id INTEGER, foreign key(tracker_id) references trackers(id), foreign key(report_id) references reports(id));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }

    private boolean existReport(SQLiteDatabase db, long reportId) {
        return exist(db,"reports",reportId);
    }

    private boolean existApplication(SQLiteDatabase db, String packageName) {
        String[] columns = {"package"};
        String where = "package = ?";
        String[] whereArgs = {packageName};
        Cursor cursor = db.query("applications",columns,where,whereArgs,null,null,null);
        boolean exist = cursor.getCount() != 0;
        cursor.close();
        return exist;
    }

    private boolean existTracker(SQLiteDatabase db, long trackerId) {
        return exist(db,"trackers",trackerId);
    }

    private boolean exist(SQLiteDatabase db, String table, long id) {
        if(id == -1)
            return false;
        String[] columns = {"id"};
        String where = "id = ?";
        String[] whereArgs = {String.valueOf(id)};
        Cursor cursor = db.query(table,columns,where,whereArgs,null,null,null);
        boolean exist = cursor.getCount() != 0;
        cursor.close();
        return exist;
    }

    private void  insertOrUpdateTracker(SQLiteDatabase db,Tracker tracker) {
        ContentValues values = new ContentValues();

        values.put("name",tracker.name);
        values.put("code_signature",tracker.codeSignature);
        values.put("network_signature",tracker.networkSignature);
        values.put("website",tracker.website);
        values.put("description",tracker.description);
        values.put("creation_date",tracker.creationDate.getTimeInMillis());

        if(!existTracker(db,tracker.id)) {
            values.put("id",tracker.id);
            db.insert("trackers", null, values);
        }
        else {
            String where = "id = ?";
            String[] whereArgs = {String.valueOf(tracker.id)};
            db.update("trackers",values,where,whereArgs);
        }
    }

    void insertOrUpdateApplication(Application application) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("package", application.packageName);
        values.put("name",application.name);
        values.put("creator",application.creator);

        if(!existApplication(db, application.packageName)) {
            db.insert("applications", null, values);
        } else {
            String where = "package = ?";
            String[] whereArgs = {application.packageName};
            db.update("applications",values,where,whereArgs);
        }

        String[] columns = {"id"};
        String where = "package = ?";
        String[] whereArgs = {application.packageName};
        Cursor cursor = db.query("applications",columns,where,whereArgs,null,null,null);
        if(cursor.moveToFirst()) {
            application.id = cursor.getLong(0);
            cursor.close();
        }

        for(Report report : application.reports) {
            insertOrUpdateReport(db,report,application.id);
        }
    }

    private void insertOrUpdateReport(SQLiteDatabase db, Report report, long appId) {
        ContentValues values = new ContentValues();

        values.put("creation",report.creationDate.getTimeInMillis());
        values.put("updateat",report.updateDate.getTimeInMillis());
        values.put("downloads",report.downloads);
        values.put("version",report.version);
        values.put("version_code",report.versionCode);
        values.put("app_id",appId);

        if(!existReport(db,report.id)) {
            values.put("id",report.id);
            db.insert("reports", null, values);
        }
        else {
            String where = "id = ?";
            String[] whereArgs = {String.valueOf(report.id)};
            db.update("reports",values,where,whereArgs);
        }
        removeTrackers(report.id);
        for(Long tracker : report.trackers) {
            insertTrackerReport(db, tracker, report.id);
        }
    }

    private void removeTrackers(long reportId) {
        String where = "report_id = ?";
        String[] whereArgs = {String.valueOf(reportId)};
        getWritableDatabase().delete("trackers_reports",where,whereArgs);
    }

    private void insertTrackerReport(SQLiteDatabase db, long trackerId, long reportId) {
        ContentValues values = new ContentValues();
        values.put("report_id",reportId);
        values.put("tracker_id",trackerId);
        db.insert("trackers_reports",null,values);
    }

    public Report getReportFor(String packageName, String version) {
        String[] columns = {"id"};
        String where = "package = ?";
        String[] whereArgs = {packageName};
        Cursor cursor = getReadableDatabase().query("applications",columns,where,whereArgs,null,null,null);
        if(cursor.moveToFirst()) {
            long appId = cursor.getLong(0);
            cursor.close();
            where = "app_id = ? and version = ?";
            whereArgs = new String[2];
            whereArgs[0] = String.valueOf(appId);
            whereArgs[1] = version;
            String order = "id ASC";
            cursor = getReadableDatabase().query("reports",columns,where,whereArgs,null,null,order);
            long reportId;
            if(cursor.moveToFirst()) {
                reportId = cursor.getLong(0);
            } else {
                columns = new String[2];
                columns[0] = "id";
                columns[1] = "creation";
                where = "app_id = ?";
                whereArgs = new String[1];
                whereArgs[0] = String.valueOf(appId);
                order = "creation DESC";
                //search a recent reports
                cursor = getReadableDatabase().query("reports",columns,where,whereArgs,null,null,order);
                if(cursor.moveToFirst()) {
                    reportId = cursor.getLong(0);
                } else {
                    return null;
                }
            }
            return getReport(reportId);

        } else {
            cursor.close();
            return null;
        }
    }

    private Report getReport(long reportId) {
        String where = "id = ?";
        String[] whereArgs = {String.valueOf(reportId)};
        Cursor cursor = getReadableDatabase().query("reports",null,where,whereArgs,null,null,null);
        //get report
        if(!cursor.moveToFirst())
            return null;

        Report report = new Report();
        int col = 0;
        report.id = cursor.getLong(col++);
        long creation = cursor.getLong(col++);
        report.creationDate = Calendar.getInstance();
        report.creationDate.setTimeInMillis(creation);
        long update = cursor.getLong(col++);
        report.updateDate = Calendar.getInstance();
        report.updateDate.setTimeInMillis(update);
        report.downloads = cursor.getString(col++);
        report.version = cursor.getString(col++);
        report.versionCode = cursor.getLong(col++);
        report.appId = cursor.getLong(col);

        report.trackers = new HashSet<>();
        where = "report_id = ?";
        String[] columns = {"tracker_id"};
        String order = "tracker_id DESC";
        cursor = getReadableDatabase().query("trackers_reports",columns,where,whereArgs,null,null,order);
        //get trackersIds
        while (cursor.moveToNext()) {
            report.trackers.add(cursor.getLong(0));
        }
        cursor.close();
        return report;
    }

    public String getCreator(long applicationId) {
        String[] columns = {"creator"};
        String where = "id = ?";
        String[] whereArgs = {String.valueOf(applicationId)};
        Cursor cursor = getReadableDatabase().query("applications",columns,where,whereArgs,null,null,null);
        String creator;
        if(cursor.moveToFirst())
            creator = cursor.getString(0);
        else
            creator = "";
        cursor.close();
        return creator;
    }

    private Tracker getTracker(long trackerId) {
        String where = "id = ?";
        String[] whereArgs = {String.valueOf(trackerId)};
        Cursor cursor = getReadableDatabase().query("trackers",null,where,whereArgs,null,null,null,null);
        Tracker tracker = null;
        if(cursor.moveToFirst())
        {
            tracker = new Tracker();
            int col = 0;
            tracker.id = cursor.getLong(col++);
            tracker.name = cursor.getString(col++);
            long creation = cursor.getLong(col++);
            tracker.creationDate = Calendar.getInstance();
            tracker.creationDate.setTimeInMillis(creation);
            tracker.codeSignature = cursor.getString(col++);
            tracker.networkSignature = cursor.getString(col++);
            tracker.website = cursor.getString(col++);
            tracker.description = cursor.getString(col);

        }
        cursor.close();
        return tracker;
    }

    public Set<Tracker> getTrackers(Set<Long> trackers_id) {
        Set<Tracker> trackers = new HashSet<>();
        for(Long trackerId : trackers_id) {
            Tracker tracker = getTracker(trackerId);
            trackers.add(tracker);
        }
        return trackers;
    }

    void insertOrUpdateTrackers(List<Tracker> trackersList) {
        SQLiteDatabase db = getWritableDatabase();
        for(Tracker tracker : trackersList) {
            insertOrUpdateTracker(db,tracker);
        }
    }
}
