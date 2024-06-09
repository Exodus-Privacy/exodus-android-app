package org.eu.exodus_privacy.exodusprivacy

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.eu.exodus_privacy.exodusprivacy.manager.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.manager.packageinfo.ExodusPackageRepository
import org.eu.exodus_privacy.exodusprivacy.manager.sync.SyncManager
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
    private val currentSize: MutableStateFlow<Int> = MutableStateFlow(1)

    private var networkConnected: Boolean = false

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var exodusPackageRepository: ExodusPackageRepository

    @Inject
    lateinit var exodusAPIRepository: ExodusAPIRepository

    @Inject
    lateinit var exodusDatabaseRepository: ExodusDatabaseRepository

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var notificationChannel: NotificationChannelCompat

    override fun onCreate() {
        super.onCreate()

        lifecycleScope.launch {
            networkManager.isOnline.collect { connected ->
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

        val numberOfInstalledPackages = exodusPackageRepository.getValidPackageList().size

        if (networkConnected) {
            IS_SERVICE_RUNNING = true
            val notificationPermGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

            if (notificationPermGranted) {
                Log.d(TAG, "Permission to post notification was granted.")

                // Create notification channels on post-nougat devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.createNotificationChannel(notificationChannel)
                }

                serviceScope.launch {
                    currentSize.collectIndexed { index, current ->
                        notificationManager.notify(
                            SERVICE_ID,
                            createNotification(
                                currentSize = current,
                                totalSize = numberOfInstalledPackages,
                                cancellable = !firstTime && index == 0,
                                context = this@ExodusUpdateService,
                            ),
                        )
                    }
                }
            }

            // Update all database
            updateAllDatabase()
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
        context: Context,
    ): Notification {
        val builder = setUpNotification(
            currentSize,
            totalSize,
            cancellable,
            context,
        )
        return builder.build()
    }

    private fun setUpNotification(
        currentSize: Int,
        totalSize: Int,
        cancellable: Boolean,
        context: Context,
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
                    totalSize + 1,
                ),
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

    private fun updateAllDatabase() {
        Toast.makeText(
            this,
            getString(R.string.fetching_apps),
            Toast.LENGTH_SHORT,
        ).show()
        serviceScope.launch {
            syncManager.sync(
                onTrackerSyncDone = {
                    // Show a different notification if possible
                },
                onAppSync = {
                    currentSize.update { it + 1 }
                },
            )
            stopService()
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
}
