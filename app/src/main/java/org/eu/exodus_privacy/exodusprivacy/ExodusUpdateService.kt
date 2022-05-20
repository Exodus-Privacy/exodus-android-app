package org.eu.exodus_privacy.exodusprivacy

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
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
import org.eu.exodus_privacy.exodusprivacy.objects.Status
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@AndroidEntryPoint
class ExodusUpdateService : LifecycleService() {

    companion object {
        const val SERVICE_ID = 1
        const val START_SERVICE = "start_service"
        const val STOP_SERVICE = "stop_service"
    }

    private val TAG = ExodusUpdateService::class.java.simpleName

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    private val currentSize: MutableLiveData<Int> = MutableLiveData(1)
    private val dbStatus: MutableLiveData<Status> = MutableLiveData()

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                START_SERVICE -> {
                    // Construct an ongoing notification and start the service
                    startForeground(
                        SERVICE_ID,
                        createNotification(currentSize.value!!, applicationList.size)
                    )

                    // Do the initial setup
                    doInitialSetup()

                    currentSize.observe(this) { current ->
                        notificationManager.notify(
                            SERVICE_ID,
                            createNotification(current, applicationList.size)
                        )
                    }
                }
                STOP_SERVICE -> {
                    stopForeground(true)
                    stopSelf()
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
    }

    private fun createNotification(currentSize: Int, totalSize: Int): Notification {
        return notificationBuilder
            .setContentTitle(
                getString(
                    R.string.updating_database,
                    currentSize + 1,
                    totalSize
                )
            )
            .setProgress(totalSize + 1, currentSize, false)
            .build()
    }

    private fun doInitialSetup() {
        serviceScope.launch {
            Log.d(TAG, "Refreshing trackers database")
            dbStatus.postValue(Status.RUNNING_TRACKER)
            fetchAndSaveTrackers()
        }.invokeOnCompletion { trackerThrow ->
            if (trackerThrow == null) {
                dbStatus.postValue(Status.COMPLETED_TRACKER)
                serviceScope.launch {
                    Log.d(TAG, "Refreshing applications database")
                    dbStatus.postValue(Status.RUNNING_APPS)
                    fetchAndSaveApps()
                }.invokeOnCompletion { appsThrow ->
                    if (appsThrow == null) {
                        dbStatus.postValue(Status.COMPLETED_APPS)
                        serviceScope.launch {
                            dataStoreModule.saveAppSetup(true)
                            // We are done, gracefully exit!
                            stopForeground(true)
                            stopSelf()
                        }
                    } else {
                        dbStatus.postValue(Status.FAILED_APPS)
                        Log.d(TAG, appsThrow.stackTrace.toString())
                    }
                }
            } else {
                dbStatus.postValue(Status.FAILED_TRACKER)
                Log.d(TAG, trackerThrow.stackTrace.toString())
            }
        }
    }

    private suspend fun fetchAndSaveTrackers() {
        val trackersList = exodusAPIRepository.getAllTrackers()
        for ((key, value) in trackersList.trackers) {
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
            exodusDatabaseRepository.saveTrackerData(trackerData)
        }
    }

    private suspend fun fetchAndSaveApps() {
        val exodusAppList = mutableListOf<ExodusApplication>()
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
            val trackersList =
                exodusDatabaseRepository.getTrackers(latestExodusApp.trackers)
            val exodusApp = ExodusApplication(
                app.packageName,
                app.name,
                app.icon,
                app.versionName,
                app.versionCode,
                app.permissions,
                latestExodusApp.version_name,
                if (latestExodusApp.version_code.isNotBlank()) latestExodusApp.version_code.toLong() else 0L,
                trackersList,
                app.source,
                latestExodusApp.report,
                latestExodusApp.updated
            )
            exodusAppList.add(exodusApp)
            currentSize.postValue(currentSize.value!! + 1)
        }
        // Save the generated data into database
        exodusAppList.forEach {
            exodusDatabaseRepository.saveApp(it)
        }
    }
}
