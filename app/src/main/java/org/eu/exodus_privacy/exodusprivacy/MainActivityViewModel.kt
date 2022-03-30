package org.eu.exodus_privacy.exodusprivacy

import android.util.Log
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

    fun doInitialSetup() {
        val fetchAndSaveTrackers = viewModelScope.launch {
            Log.d(TAG, "Refreshing trackers database")
            fetchAndSaveTrackers()
        }
        fetchAndSaveTrackers.invokeOnCompletion {
            if (it == null) {
                viewModelScope.launch {
                    Log.d(TAG, "Refreshing applications database")
                    fetchAndSaveApps()
                    dataStoreModule.saveAppSetup(true)
                }
            } else {
                Log.d(TAG, it.stackTrace.toString())
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
        // Generate data using the latest app version
        applicationList.forEach { app ->
            val appDetailList = exodusAPIRepository.getAppDetails(app.packageName).toMutableList()
            if (appDetailList.isNotEmpty()) {
                val latestExodusApp = appDetailList.maxByOrNull { it.version_code } ?: AppDetails()
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
