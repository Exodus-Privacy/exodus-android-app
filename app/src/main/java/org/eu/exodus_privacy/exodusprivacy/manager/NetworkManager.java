/*
 * Copyright (C) 2018 Anthony Chomienne, anthony@mob-dev.fr
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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import org.eu.exodus_privacy.exodusprivacy.BuildConfig;
import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.Utils;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import org.eu.exodus_privacy.exodusprivacy.objects.Application;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

import static android.content.Context.MODE_PRIVATE;

/*
    Singleton that handle all network connection
*/
public class NetworkManager {

    private static NetworkManager instance;
    private NetworkProcessingThread thread;

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        if (instance == null)
            instance = new NetworkManager();
        return instance;
    }


    public void getSingleReport(Context context, NetworkListener listener, String packageName) {
        Message mes = new Message();
        mes.type = Message_Type.GET_SINGLE_REPORT;
        mes.context = context;
        mes.listener = listener;
        mes.args = new Bundle();
        mes.args.putString("package", packageName);
        addMessageToQueue(mes);
    }

    public void getReports(Context context, NetworkListener listener, ArrayList<String> packageList) {
        Message mes = new Message();
        mes.type = Message_Type.GET_REPORTS;
        mes.context = context;
        mes.listener = listener;
        mes.args = new Bundle();
        mes.args.putStringArrayList("packages", packageList);
        addMessageToQueue(mes);
    }

    private void addMessageToQueue(Message mes) {
        if (thread == null || thread.getState() == Thread.State.TERMINATED || !thread.isRunning)
            thread = new NetworkProcessingThread();
        thread.queueMessage(mes);
        if (thread.getState() == Thread.State.NEW)
            thread.start();
    }

    private enum Message_Type {
        GET_REPORTS,
        GET_SINGLE_REPORT,
        UNKNOWN
    }

    private static class NetworkProcessingThread extends Thread {
        private final String domain = Utils.getDomain();
        private final String apiUrl = "https://" + domain + "/api/";
        private final List<Message> messageQueue;
        private final Semaphore sem;
        boolean isRunning;

        NetworkProcessingThread() {
            messageQueue = new ArrayList<>();
            sem = new Semaphore(0, true);
        }

        void queueMessage(Message mes) {
            messageQueue.add(mes);
            sem.release();
        }

        @Override
        public void run() {
            isRunning = true;
            Message mes;
            while (isRunning) {
                try {
                    sem.acquire();
                    mes = messageQueue.remove(0);
                    switch (mes.type) {
                        case GET_REPORTS:
                            getTrackers(mes);
                            getApplications(mes);
                            SharedPreferences sharedPreferences = mes.context.getSharedPreferences(Utils.APP_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Utils.LAST_REFRESH, Utils.dateToString(new Date()));
                            editor.apply();
                            break;
                        case GET_SINGLE_REPORT:
                            String packageName = mes.args.getString("package");
                            Application application = getSingleReport(mes, packageName);
                            mes.listener.onSuccess(application);
                            break;
                        default:
                            break;
                    }
                } catch (InterruptedException e) {
                    isRunning = false;
                }
            }
        }

        private JSONObject makeDataRequest(Context context, NetworkListener listener, URL url) {

            if (!isConnectedToInternet(context)) {
                listener.onError(context.getString(R.string.not_connected));
                return null;
            }

            InputStream inStream;
            HttpURLConnection urlConnection;
            boolean success = true;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Authorization", "Token " + BuildConfig.EXODUS_API_KEY);
                urlConnection.setDoInput(true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            try {
                inStream = urlConnection.getInputStream();
            } catch (Exception e) {

                e.printStackTrace();
                success = false;
                inStream = urlConnection.getErrorStream();

            }
            JSONObject object = null;
            if (success) {
                String jsonStr = getJSON(inStream);
                try {
                    object = new JSONObject(jsonStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (inStream != null)
                    inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return object;
        }

        private void getTrackers(Message mes) {
            mes.listener.onProgress(R.string.get_trackers_connection, 0, 0);
            URL url;
            try {
                url = new URL(apiUrl + "trackers");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            JSONObject object = makeDataRequest(mes.context, mes.listener, url);
            mes.listener.onProgress(R.string.get_trackers, 0, 0);

            if (object != null) {
                try {
                    JSONObject trackers = object.getJSONObject("trackers");
                    List<Tracker> trackersList = new ArrayList<>();
                    JSONArray trackerNames = trackers.names();
                    if (trackerNames != null) {
                        for (int i = 0; i < trackerNames.length(); i++) {
                            mes.listener.onProgress(R.string.parse_trackers, i + 1, trackerNames.length());
                            String trackerId = trackerNames.get(i).toString();
                            JSONObject tracker = trackers.getJSONObject(trackerId);
                            Tracker track = parseTracker(tracker, trackerId);
                            trackersList.add(track);
                            if (trackersList.size() == 20) {
                                DatabaseManager.getInstance(mes.context).insertOrUpdateTrackers(trackersList);
                                trackersList.clear();
                            }
                        }
                    }
                    if (!trackersList.isEmpty())
                        DatabaseManager.getInstance(mes.context).insertOrUpdateTrackers(trackersList);
                    trackersList.clear();
                } catch (JSONException e) {
                    mes.listener.onError(mes.context.getString(R.string.json_error));
                }
            }
        }


        private void getApplications(Message mes) {
            mes.listener.onProgress(R.string.get_reports_connection, 0, 0);
            URL url;
            try {
                url = new URL(apiUrl + "applications?option=short");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            JSONObject object = makeDataRequest(mes.context, mes.listener, url);
            mes.listener.onProgress(R.string.get_reports, 0, 0);


            if (object != null) {
                Map<String, Map<String, String>> handles = new HashMap<>();
                ArrayList<String> packages = mes.args.getStringArrayList("packages");
                if (packages == null)
                    return;

                try {
                    JSONArray applications = object.getJSONArray("applications");

                    //manage handles map (handle,UAID)
                    for (int i = 0; i < applications.length(); i++) {
                        JSONObject app = applications.getJSONObject(i);
                        String handle = app.getString("handle");
                        String auid = app.getString("app_uid");
                        String source = app.getString("source");
                        Map<String, String> sources = handles.get(handle);
                        if (sources == null)
                            sources = new HashMap<>();

                        sources.put(source, auid);
                        if (packages.contains(handle))
                            handles.put(handle, sources);
                    }

                    //remove app not analyzed by Exodus
                    packages.retainAll(handles.keySet());

                    // Add some random packages to avoid tracking
                    SecureRandom rand = new SecureRandom();
                    int alea = rand.nextInt(120) % 10 + 11;
                    for (int i = 0; i < alea; i++) {
                        int val = rand.nextInt(applications.length());
                        JSONObject app = applications.getJSONObject(val);
                        String handle = app.getString("handle");
                        handles.put(handle, new HashMap<>());
                        packages.add(handle);
                    }
                    //shuffle the list
                    Collections.shuffle(packages);
                    object.remove("applications");

                } catch (JSONException e) {
                    e.printStackTrace();
                    mes.listener.onError(mes.context.getString(R.string.json_error));
                }
                getReports(mes, handles, packages);
            }
            mes.listener.onSuccess(null);
        }

        private void getReports(Message mes, Map<String, Map<String, String>> handles, ArrayList<String> packages) {
            for (int i = 0; i < packages.size(); i++) {
                mes.listener.onProgress(R.string.parse_application, i + 1, packages.size());
                getReport(mes, packages.get(i), handles.get(packages.get(i)));
            }
        }

        private void getReport(Message mes, String handle, Map<String, String> sources) {
            URL url;
            try {
                url = new URL(apiUrl + "search/" + handle);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            JSONObject object = makeDataRequest(mes.context, mes.listener, url);

            if (object != null) {
                try {
                    JSONObject application = object.getJSONObject(handle);
                    ArrayList<String> packages = mes.args.getStringArrayList("packages");
                    if (packages != null && packages.contains(handle)) {
                        Application app = parseApplication(application, handle);
                        app.sources = sources;
                        DatabaseManager.getInstance(mes.context).insertOrUpdateApplication(app);
                    }
                } catch (JSONException e) {
                    mes.listener.onError(mes.context.getString(R.string.json_error));
                }
            }
        }


        private Application getSingleReport(Message mes, String handle) {
            URL url;
            try {
                url = new URL(apiUrl + "search/" + handle);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            JSONObject object = makeDataRequest(mes.context, mes.listener, url);
            if (object != null) {
                try {
                    if (handle != null) {
                        JSONObject application = object.getJSONObject(handle);
                        return parseApplication(application, handle);
                    }
                } catch (JSONException e) {
                    mes.listener.onError(mes.context.getString(R.string.json_error));
                }
            }
            return null;
        }

        private Application parseApplication(JSONObject object, String packageName) throws JSONException {
            Application application = new Application();
            application.packageName = packageName;
            application.creator = object.getString("creator");
            application.name = object.getString("name");

            //parse Report
            application.reports = new HashSet<>();
            JSONArray reports = object.getJSONArray("reports");
            for (int i = 0; i < reports.length(); i++) {
                Report report = parseReport(reports.getJSONObject(i));
                application.reports.add(report);
            }
            return application;
        }

        private Report parseReport(JSONObject object) throws JSONException {
            Report report = new Report();
            report.id = object.getLong("id");
            report.downloads = object.getString("downloads");
            report.version = object.getString("version");
            report.source = object.getString("source");
            if (!object.getString("version_code").isEmpty())
                report.versionCode = Long.parseLong(object.getString("version_code"));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            try {
                report.updateDate = Calendar.getInstance();
                report.updateDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = dateFormat.parse(object.getString("updated_at"));
                if (date != null) {
                    report.updateDate.setTime(date);
                }
                report.updateDate.set(Calendar.MILLISECOND, 0);

                report.creationDate = Calendar.getInstance();
                report.creationDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                report.creationDate.setTime(date!=null?date:new Date());
                date = dateFormat.parse(object.getString("creation_date"));
                if (date != null) {
                    report.creationDate.setTime(date);
                }
                report.creationDate.set(Calendar.MILLISECOND, 0);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JSONArray trackersArray = object.getJSONArray("trackers");
            report.trackers = new HashSet<>();
            for (int i = 0; i < trackersArray.length(); i++)
                report.trackers.add(trackersArray.getLong(i));
            return report;
        }

        private Tracker parseTracker(JSONObject object, String trackerId) throws JSONException {
            Tracker tracker = new Tracker();
            tracker.id = Long.parseLong(trackerId);
            tracker.website = object.getString("website");
            tracker.name = object.getString("name");
            tracker.description = object.getString("description");
            tracker.networkSignature = object.getString("network_signature");
            tracker.codeSignature = object.getString("code_signature");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                tracker.creationDate = Calendar.getInstance();
                tracker.creationDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = dateFormat.parse(object.getString("creation_date"));
                if (date != null) {
                    tracker.creationDate.setTime(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return tracker;
        }

        private String getJSON(InputStream stream) {
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            boolean isReading = true;
            String data;
            StringBuilder builder = new StringBuilder();
            //get all data in a String
            do {
                try {
                    data = br.readLine();
                    if (data != null)
                        builder.append(data);
                    else
                        isReading = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    isReading = false;
                }
            } while (isReading);
            return builder.toString();
        }

        private boolean isConnectedToInternet(Context context) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

                if (capabilities != null) {
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                }
            } else {
                if (connectivityManager == null)
                    return false;
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    NetworkInfo.State networkState = networkInfo.getState();
                    return networkState.equals(NetworkInfo.State.CONNECTED);
                }
            }
            return false;
        }
    }

    private static class Message {
        Message_Type type = Message_Type.UNKNOWN;
        Bundle args = new Bundle();
        NetworkListener listener;
        Context context;
    }

}
