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

    private val TAG = ExodusDatabaseRepository::class.java.simpleName

    suspend fun saveTrackerData(trackerData: TrackerData) {
        withContext(ioDispatcher) {
            Log.d(TAG, "Adding Tracker ${trackerData.name} to DB.")
            trackerDataDao.insertTrackerData(trackerData)
        }
    }

    suspend fun getTracker(id: Int): TrackerData {
        return withContext(ioDispatcher) {
            Log.d(TAG, "Querying tracker by id: $id.")
            val list = trackerDataDao.queryTrackerById(id)
            return@withContext if (list.isNotEmpty() && list.size == 1) {
                list[0]
            } else {
                Log.w(TAG, "Failed to get trackers from DB returning empty TrackerData().")
                TrackerData()
            }
        }
    }

    fun getAllTrackers(): LiveData<List<TrackerData>> {
        Log.d(TAG, "Querying all trackers as live data.")
        return trackerDataDao.queryAllTrackers()
    }

    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData> {
        return withContext(ioDispatcher) {
            Log.d(TAG, "Querying trackers by list of ids: $listOfID.")
            trackerDataDao.queryTrackersByIdList(listOfID)
        }
    }

    suspend fun deleteTrackerData(trackerData: TrackerData) {
        withContext(ioDispatcher) {
            trackerDataDao.deleteTrackerData(trackerData)
        }
    }

    suspend fun saveApp(exodusApplication: ExodusApplication) {
        withContext(ioDispatcher) {
            Log.d(TAG, "Adding app ${exodusApplication.name} to DB.")
            exodusApplicationDao.insertApp(exodusApplication)
        }
    }

    suspend fun getApp(packageName: String): ExodusApplication {
        return withContext(ioDispatcher) {
            Log.d(TAG, "Querying app $packageName.")
            val list = exodusApplicationDao.queryApp(packageName)
            return@withContext if (list.isNotEmpty() && list.size == 1) {
                list[0]
            } else {
                Log.d(
                    TAG,
                    "Failed to get ExodusApplication from DB returning empty ExodusApplication().",
                )
                ExodusApplication()
            }
        }
    }

    suspend fun getApps(listOfPackages: List<String>): List<ExodusApplication> {
        return withContext(ioDispatcher) {
            Log.d(TAG, "Querying apps by list of package names: $listOfPackages.")
            exodusApplicationDao.queryApps(listOfPackages)
        }
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
}
