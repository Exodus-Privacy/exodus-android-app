package org.eu.exodus_privacy.exodusprivacy.manager.database

import androidx.lifecycle.LiveData
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import javax.inject.Inject

class ExodusDatabaseRepository @Inject constructor(
    exodusDatabase: ExodusDatabase
) {

    private val trackerDataDao = exodusDatabase.trackerDataDao()
    private val exodusApplicationDao = exodusDatabase.exodusApplicationDao()

    suspend fun saveTrackerData(trackerData: TrackerData) {
        return trackerDataDao.saveTrackerData(trackerData)
    }

    suspend fun getTrackers(id: Int): TrackerData {
        val list = trackerDataDao.getTrackers(id)
        return if (list.isNotEmpty() && list.size == 1) list[0] else TrackerData()
    }

    fun getAllTrackers(): LiveData<List<TrackerData>> {
        return trackerDataDao.getAllTrackers()
    }

    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData> {
        return trackerDataDao.getTrackers(listOfID)
    }

    suspend fun deleteTrackerData(trackerData: TrackerData) {
        return trackerDataDao.deleteTrackerData(trackerData)
    }

    suspend fun saveApp(exodusApplication: ExodusApplication) {
        return exodusApplicationDao.insertApp(exodusApplication)
    }

    suspend fun getApp(packageName: String): ExodusApplication {
        val list = exodusApplicationDao.queryApp(packageName)
        return if (list.isNotEmpty() && list.size == 1) list[0] else ExodusApplication()
    }

    suspend fun getApps(listOfPackages: List<String>): List<ExodusApplication> {
        return exodusApplicationDao.queryApps(listOfPackages)
    }

    fun getAllApps(): LiveData<List<ExodusApplication>> {
        return exodusApplicationDao.queryAllApps()
    }
}
