package org.eu.exodus_privacy.exodusprivacy

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val applicationList: MutableList<Application>,
    private val exodusAPIRepository: ExodusAPIRepository,
    private val exodusDatabaseRepository: ExodusDatabaseRepository,
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    private val TAG = MainActivityViewModel::class.java.simpleName

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()
    val appSetup = dataStoreModule.appSetup.asLiveData()

    val dbStatus: MutableLiveData<Status> = MutableLiveData()

    fun doInitialSetup() {
        viewModelScope.launch {
            Log.d(TAG, "Refreshing trackers database")
            dbStatus.value = Status.RUNNING_TRACKER
            fetchAndSaveTrackers()
        }.invokeOnCompletion { trackerThrow ->
            if (trackerThrow == null) {
                dbStatus.value = Status.COMPLETED_TRACKER
                viewModelScope.launch {
                    Log.d(TAG, "Refreshing applications database")
                    dbStatus.value = Status.RUNNING_APPS
                    fetchAndSaveApps()
                }.invokeOnCompletion { appsThrow ->
                    if (appsThrow == null) {
                        dbStatus.value = Status.COMPLETED_APPS
                        viewModelScope.launch { dataStoreModule.saveAppSetup(true) }
                    } else {
                        dbStatus.value = Status.FAILED_APPS
                        Log.d(TAG, appsThrow.stackTrace.toString())
                    }
                }
            } else {
                dbStatus.value = Status.FAILED_TRACKER
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
            if (appDetailList.isNotEmpty()) {

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
                    latestExodusApp.version_code.toLong(),
                    trackersList
                )
                exodusAppList.add(exodusApp)
            }
        }
        // Save the generated data into database
        exodusAppList.forEach {
            exodusDatabaseRepository.saveApp(it)
        }
    }
}
