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
    @IoDispatcher val ioDispatcher: CoroutineDispatcher
) {

    private val trackerDataDao = exodusDatabase.trackerDataDao()
    private val exodusApplicationDao = exodusDatabase.exodusApplicationDao()

    private val TAG = ExodusDatabaseRepository::class.java.simpleName

    suspend fun saveTrackerData(trackerData: TrackerData) {
        return withContext(ioDispatcher) {
            trackerDataDao.saveTrackerData(trackerData)
        }
    }

    suspend fun getTrackers(id: Int): TrackerData {
        return withContext(ioDispatcher) {
            val list = trackerDataDao.getTrackers(id)
            return@withContext if (list.isNotEmpty() && list.size == 1) {
                list[0]
            } else {
                Log.d(TAG, "Failed to get trackers from DB returning empty TrackerData()")
                TrackerData()
            }
        }
    }

    fun getAllTrackers(): LiveData<List<TrackerData>> {
        return trackerDataDao.getAllTrackers()
    }

    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData> {
        return withContext(ioDispatcher) {
            trackerDataDao.getTrackers(listOfID)
        }
    }

    suspend fun deleteTrackerData(trackerData: TrackerData) {
        return withContext(ioDispatcher) {
            trackerDataDao.deleteTrackerData(trackerData)
        }
    }

    suspend fun saveApp(exodusApplication: ExodusApplication) {
        return withContext(ioDispatcher) {
            exodusApplicationDao.insertApp(exodusApplication)
        }
    }

    suspend fun getApp(packageName: String): ExodusApplication {
        return withContext(ioDispatcher) {
            val list = exodusApplicationDao.queryApp(packageName)
            return@withContext if (list.isNotEmpty() && list.size == 1) {
                list[0]
            } else {
                Log.d(TAG, "Failed to get ExodusApplication from DB returning empty ExodusApplication()")
                ExodusApplication()
            }
        }
    }

    suspend fun getApps(listOfPackages: List<String>): List<ExodusApplication> {
        return withContext(ioDispatcher) {
            exodusApplicationDao.queryApps(listOfPackages)
        }
    }

    fun getAllApps(): LiveData<List<ExodusApplication>> {
        return exodusApplicationDao.queryAllApps()
    }
}
