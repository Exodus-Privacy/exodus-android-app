package org.eu.exodus_privacy.exodusprivacy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.AppDetails
import org.eu.exodus_privacy.exodusprivacy.objects.Application
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@AndroidEntryPoint
class ExodusUpdateService : LifecycleService() {

    companion object {
        var IS_SERVICE_RUNNING = false
        const val SERVICE_ID = 1
        const val FIRST_TIME_START_SERVICE = "first_time_start_service"
        const val START_SERVICE = "start_service"
        const val STOP_SERVICE = "stop_service"
    }

    private val TAG = ExodusUpdateService::class.java.simpleName

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    // Tracker and Apps
    private val trackersList = mutableListOf<TrackerData>()
    private val appList = mutableListOf<ExodusApplication>()
    private val currentSize: MutableLiveData<Int> = MutableLiveData(1)

    // Inject required modules
    @Inject
    lateinit var applicationList: MutableList<Application>

    @Inject
    lateinit var exodusAPIRepository: ExodusAPIRepository

    @Inject
    lateinit var exodusDatabaseRepository: ExodusDatabaseRepository

    @Inject
    lateinit var dataStoreModule: DataStoreModule

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationChannel: NotificationChannel

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                FIRST_TIME_START_SERVICE -> {
                    IS_SERVICE_RUNNING = true

                    // Create notification channel on post-nougat devices
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManager.createNotificationChannel(notificationChannel)
                    }

                    // Construct an ongoing notification and start the service
                    startForeground(
                        SERVICE_ID,
                        createNotification(currentSize.value!!, applicationList.size, false, this)
                    )

                    // Do the initial setup
                    updateAllDatabase(true)

                    currentSize.observe(this) { current ->
                        notificationManager.notify(
                            SERVICE_ID,
                            createNotification(current, applicationList.size, false, this)
                        )
                    }
                }
                START_SERVICE -> {
                    IS_SERVICE_RUNNING = true

                    // Construct an ongoing notification and start the service
                    startForeground(
                        SERVICE_ID,
                        createNotification(currentSize.value!!, applicationList.size, true, this)
                    )

                    // Update all database
                    updateAllDatabase(false)

                    currentSize.observe(this) { current ->
                        notificationManager.notify(
                            SERVICE_ID,
                            createNotification(current, applicationList.size, false, this)
                        )
                    }
                }
                STOP_SERVICE -> {
                    stopForeground(true)
                    stopSelf()
                    notificationManager.cancel(SERVICE_ID)
                    IS_SERVICE_RUNNING = false
                }
                else -> {
                    Log.d(TAG, "Got an unhandled action: ${it.action}")
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        IS_SERVICE_RUNNING = false
    }

    private fun createNotification(
        currentSize: Int,
        totalSize: Int,
        cancellable: Boolean,
        context: Context
    ): Notification {
        val builder = notificationBuilder
            .setContentTitle(
                getString(
                    R.string.updating_database,
                    currentSize,
                    totalSize + 1
                )
            )
            .setProgress(totalSize + 1, currentSize, false)
        if (cancellable) {
            val intent = Intent(this, ExodusUpdateService::class.java)
            intent.action = STOP_SERVICE
            val pendingIntent =
                PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(R.drawable.ic_cancel, getString(R.string.cancel), pendingIntent)
        }
        return builder.build()
    }

    private fun updateAllDatabase(firstTime: Boolean) {
        serviceScope.launch {
            Log.d(TAG, "Refreshing trackers database")
            fetchTrackers()
        }.invokeOnCompletion { trackerThrow ->
            if (trackerThrow == null) {
                serviceScope.launch {
                    Log.d(TAG, "Refreshing applications database")
                    fetchApps()
                }.invokeOnCompletion { appsThrow ->
                    if (appsThrow == null) {
                        serviceScope.launch {
                            // All data is fetched, save it
                            trackersList.forEach {
                                exodusDatabaseRepository.saveTrackerData(it)
                            }
                            appList.forEach {
                                exodusDatabaseRepository.saveApp(it)
                            }
                            if (firstTime) dataStoreModule.saveAppSetup(true)
                            // We are done, gracefully exit!
                            stopForeground(true)
                            stopSelf()
                        }
                    } else {
                        Log.d(TAG, appsThrow.stackTrace.toString())
                    }
                }
            } else {
                Log.d(TAG, trackerThrow.stackTrace.toString())
            }
        }
    }

    private suspend fun fetchTrackers() {
        val list = exodusAPIRepository.getAllTrackers()
        list.trackers.forEach { (key, value) ->
            val trackerData = TrackerData(
                key.toInt(),
                value.categories,
                value.code_signature,
                value.creation_date,
                value.description,
                value.name,
                value.network_signature,
                value.website
            )
            trackersList.add(trackerData)
        }
    }

    private suspend fun fetchApps() {
        applicationList.forEach { app ->
            val appDetailList = exodusAPIRepository.getAppDetails(app.packageName).toMutableList()
            // Look for current installed version in the list, otherwise pick the latest one
            val currentApp =
                appDetailList.filter { it.version_code.toLong() == app.versionCode }
            val latestExodusApp = if (currentApp.isNotEmpty()) {
                currentApp[0]
            } else {
                appDetailList.maxByOrNull { it.version_code.toLong() } ?: AppDetails()
            }

            // Create and save app data with proper tracker info
            val exodusApp = ExodusApplication(
                app.packageName,
                app.name,
                app.icon,
                app.versionName,
                app.versionCode,
                app.permissions,
                latestExodusApp.version_name,
                if (latestExodusApp.version_code.isNotBlank()) latestExodusApp.version_code.toLong() else 0L,
                latestExodusApp.trackers,
                app.source,
                latestExodusApp.report,
                latestExodusApp.updated
            )
            appList.add(exodusApp)

            // Update tracker data regarding this app
            latestExodusApp.trackers.forEach { id ->
                trackersList.find { it.id == id }?.let {
                    it.presentOnDevice = true
                    it.exodusApplications.add(exodusApp.packageName)
                }
            }

            currentSize.postValue(currentSize.value!! + 1)
        }
    }
}
