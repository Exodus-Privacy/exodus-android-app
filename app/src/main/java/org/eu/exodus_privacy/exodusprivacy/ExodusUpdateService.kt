package org.eu.exodus_privacy.exodusprivacy

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.eu.exodus_privacy.exodusprivacy.manager.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.AppDetails
import org.eu.exodus_privacy.exodusprivacy.manager.packageinfo.ExodusPackageRepository
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusConfig
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusDataStoreRepository
import org.eu.exodus_privacy.exodusprivacy.objects.Application
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
    private val serviceScope = CoroutineScope(job)

    // Tracker and Apps
    private val trackersList = mutableListOf<TrackerData>()
    private val appList = mutableListOf<ExodusApplication>()
    private val currentSize: MutableLiveData<Int> = MutableLiveData(1)

    private var networkConnected: Boolean = false
    private var totalNumberOfAppsHavingTrackers = 0
    private var validPackages = listOf<PackageInfo>()
    private var notificationPermGranted = false

    // Inject required modules
    var applicationList = mutableListOf<Application>()
    private var applicationListAfterUninstall = mutableListOf<Application>()

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var exodusPackageRepository: ExodusPackageRepository

    @Inject
    lateinit var exodusAPIRepository: ExodusAPIRepository

    @Inject
    lateinit var exodusDatabaseRepository: ExodusDatabaseRepository

    @Inject
    lateinit var exodusDataStoreRepository: ExodusDataStoreRepository<ExodusConfig>

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var notificationChannel: NotificationChannelCompat

    override fun onCreate() {
        super.onCreate()

        lifecycleScope.launch {
            networkManager.networkState.collect { connected ->
                networkConnected = connected
                if (!connected) {
                    // No connection, close the service
                    Log.w(TAG, "No Internet Connection. Stopping Service.")
                    stopService()
                }
            }
        }
    }

    // Allow binding to exodus update service
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): ExodusUpdateService = this@ExodusUpdateService
    }
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                FIRST_TIME_START_SERVICE -> {
                    launchFetch(true)
                }
                START_SERVICE -> {
                    launchFetch(false)
                }
                STOP_SERVICE -> {
                    stopService()
                }
                else -> {
                    Log.w(TAG, "Got an unhandled action: ${it.action}.")
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun launchFetch(firstTime: Boolean) {
        // create list of installed packages, that are system apps or launchable

        validPackages = exodusPackageRepository.getValidPackageList()
        val numberOfInstalledPackages = validPackages.size

        if (networkConnected) {
            IS_SERVICE_RUNNING = true

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Permission to post notification was granted.")
                notificationPermGranted = true

                // Create notification channels on post-nougat devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.createNotificationChannel(notificationChannel)
                }

                notificationManager.notify(
                    SERVICE_ID,
                    createNotification(
                        currentSize.value!!,
                        numberOfInstalledPackages,
                        !firstTime,
                        this
                    )
                )
            }

            // Update all database
            updateAllDatabase(firstTime)

            if (notificationPermGranted) {
                currentSize.observe(this) { current ->
                    notificationManager.notify(
                        SERVICE_ID,
                        createNotification(current, numberOfInstalledPackages, false, this)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called.")
        stopService()
        super.onDestroy()
    }

    private fun createNotification(
        currentSize: Int,
        totalSize: Int,
        cancellable: Boolean,
        context: Context
    ): Notification {
        val builder = setUpNotification(
            currentSize,
            totalSize,
            cancellable,
            context
        )
        return builder.build()
    }

    private fun setUpNotification(
        currentSize: Int,
        totalSize: Int,
        cancellable: Boolean,
        context: Context
    ): NotificationCompat.Builder {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(notificationIntent)
            getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = notificationBuilder
            .setContentTitle(
                getString(
                    R.string.updating_database,
                    currentSize,
                    totalSize + 1
                )
            )
            .setProgress(totalSize + 1, currentSize, false)
            .setTimeoutAfter(5000L)
            .setContentIntent(notificationPendingIntent)
        if (cancellable) {
            val intent = Intent(this, ExodusUpdateService::class.java)
            intent.action = STOP_SERVICE
            val pendingIntent =
                PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(R.drawable.ic_cancel, getString(R.string.cancel), pendingIntent)
        }
        return builder
    }

    private fun updateAllDatabase(firstTime: Boolean) {
        Toast.makeText(
            this,
            getString(R.string.fetching_apps),
            Toast.LENGTH_SHORT
        ).show()
        serviceScope.launch {
            if (!firstTime) removeUninstalledApps()
            Log.d(TAG, "Refreshing trackers database.")
            fetchTrackers()
        }.invokeOnCompletion { trackerThrow ->
            if (trackerThrow == null) {
                serviceScope.launch {
                    Log.d(TAG, "Refreshing applications database.")
                    fetchApps()
                }.invokeOnCompletion { appsThrow ->
                    if (appsThrow == null) {
                        serviceScope.launch {
                            // All data is fetched, save it
                            trackersList.forEach {
                                exodusDatabaseRepository.saveTrackerData(it)
                            }
                            Log.d(TAG, "Done saving tracker data.")
                            appList.forEach {
                                exodusDatabaseRepository.saveApp(it)
                            }
                            Log.d(TAG, "Done saving app details.")
                            exodusDataStoreRepository.insertAppSetup(ExodusConfig("is_setup_complete", true))
                            // We are done, gracefully exit!
                            stopService()
                        }
                    } else {
                        Log.e(TAG, appsThrow.stackTrace.toString())
                    }
                }
            } else {
                Log.e(TAG, trackerThrow.stackTrace.toString())
            }
        }
    }

    private suspend fun fetchTrackers() {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Unable to fetch trackers.", e)
        }
    }

    private suspend fun fetchApps() {
        try {
            applicationList = exodusPackageRepository.getApplicationList(validPackages)
            applicationList.forEach { app ->
                val appDetailList =
                    exodusAPIRepository.getAppDetails(app.packageName).toMutableList()

                val remoteVersionCodes: ArrayList<String> = arrayListOf()
                val localVersionCode = app.versionCode

                appDetailList.forEach { remoteVersionCodes.add(it.version_code) }
                Log.d(TAG, "List of remote version codes for ${app.name}\n$remoteVersionCodes")
                Log.d(TAG, "Local version code for ${app.name}\n$localVersionCode")

                // Look for current installed version in the list, otherwise pick the latest one
                val currentApp =
                    appDetailList.filter { it.version_code.toLongOrZero() == app.versionCode }

                // if a matching version code was found, use this as our exodus app
                val latestExodusApp = if (currentApp.isNotEmpty()) {
                    currentApp[0]
                } else { // otherwise use highest number of version codes found
                    appDetailList.maxByOrNull { it.version_code.toLongOrZero() } ?: AppDetails()
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
                    latestExodusApp.version_code.toLongOrZero(),
                    latestExodusApp.trackers,
                    app.source,
                    latestExodusApp.report,
                    latestExodusApp.created,
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

            totalNumberOfAppsHavingTrackers = countAppsHavingTrackers(appList)
            trackersList.forEach {
                it.totalNumberOfAppsHavingTrackers = totalNumberOfAppsHavingTrackers
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to fetch apps.", e)
        }
    }

    private suspend fun removeUninstalledApps() {
        try {
            applicationListAfterUninstall =
                exodusPackageRepository.getApplicationList(validPackages)
            val packageNameListAfterUninstall = mutableListOf<String>()
            applicationListAfterUninstall.forEach { packageNameListAfterUninstall.add(it.packageName) }
            val packageNameList = exodusDatabaseRepository.getAllPackageNames().toMutableList()
            val listOfPackageNameToBeRemove = mutableListOf<String>()
            if (packageNameList.size > packageNameListAfterUninstall.size) {
                packageNameList.forEach {
                    if (!packageNameListAfterUninstall.contains(it)) {
                        listOfPackageNameToBeRemove.add(it)
                    }
                }
                exodusDatabaseRepository.deleteApps(listOfPackageNameToBeRemove)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to remove apps.", e)
        }
    }

    fun countAppsHavingTrackers(
        appList: MutableList<ExodusApplication>
    ): Int {
        return appList.count {
            it.exodusTrackers.isNotEmpty()
        }
    }

    fun serviceRuns(): Boolean {
        return IS_SERVICE_RUNNING
    }

    private fun stopService() {
        IS_SERVICE_RUNNING = false
        notificationManager.cancel(SERVICE_ID)
        job.cancel()
        stopSelf()
    }

    private fun String.toLongOrZero(): Long {
        return if (this.isNotBlank()) {
            this.toLong()
        } else {
            0L
        }
    }
}
