package org.eu.exodus_privacy.exodusprivacy.manager.database

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import javax.inject.Inject

class ExodusDatabaseRepository @Inject constructor(
    exodusDatabase: ExodusDatabase,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
) {

    private val trackerDataDao = exodusDatabase.trackerDataDao()
    private val exodusApplicationDao = exodusDatabase.exodusApplicationDao()

    suspend fun saveTrackerData(trackerData: TrackerData) {
        Log.d(TAG, "Adding Tracker ${trackerData.name} to DB.")
        trackerDataDao.insertTrackerData(trackerData)
    }

    suspend fun getTracker(id: Int): TrackerData {
        Log.d(TAG, "Querying tracker by id: $id.")
        val list = trackerDataDao.queryTrackerById(id)
        return withContext(ioDispatcher) {
            list.singleOrNull() ?: run {
                Log.d(TAG, "Failed to get trackers from DB returning empty TrackerData().")
                TrackerData()
            }
        }
    }

    fun getAllTrackers(): LiveData<List<TrackerData>> {
        Log.d(TAG, "Querying all trackers as live data.")
        return trackerDataDao.queryAllTrackers()
    }

    fun getActiveTrackers(): LiveData<List<TrackerData>> {
        Log.d(TAG, "Querying all active trackers as live data.")
        return trackerDataDao.queryActiveTrackers()
    }

    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData> {
        Log.d(TAG, "Querying trackers by list of ids: $listOfID.")
        return trackerDataDao.queryTrackersByIdList(listOfID)
    }

    suspend fun deleteTrackerData(trackerData: TrackerData) {
        trackerDataDao.deleteTrackerData(trackerData)
    }

    suspend fun saveApp(exodusApplication: ExodusApplication) {
        Log.d(TAG, "Adding app ${exodusApplication.name} to DB.")
        exodusApplicationDao.insertApp(exodusApplication)
    }

    suspend fun getApp(packageName: String): ExodusApplication {
        Log.d(TAG, "Querying app $packageName.")
        val list = exodusApplicationDao.queryApp(packageName)

        return withContext(ioDispatcher) {
            list.singleOrNull() ?: run {
                Log.d(TAG, "Failed to get ExodusApplication from DB returning empty ExodusApplication().")
                ExodusApplication()
            }
        }
    }

    suspend fun getApps(listOfPackages: List<String>): List<ExodusApplication> {
        Log.d(TAG, "Querying apps by list of package names: $listOfPackages.")
        return exodusApplicationDao.queryApps(listOfPackages)
    }

    fun getAllApps(): LiveData<List<ExodusApplication>> {
        Log.d(TAG, "Querying all apps as live data.")
        return exodusApplicationDao.queryAllApps()
    }

    suspend fun deleteApps(listOfPackages: List<String>) {
        Log.d(TAG, "Deleting all uninstalled apps.")
        return exodusApplicationDao.deleteApp(listOfPackages)
    }

    suspend fun getAllPackageNames(): List<String> {
        Log.d(TAG, "Fetching all ExodusApplication packageName from DB.")
        return exodusApplicationDao.getPackageNames()
    }

    private companion object {
        const val TAG = "ExodusDatabaseRepository"
    }
}
