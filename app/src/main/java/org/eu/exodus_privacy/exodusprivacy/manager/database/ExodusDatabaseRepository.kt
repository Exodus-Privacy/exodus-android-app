package org.eu.exodus_privacy.exodusprivacy.manager.database

import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import javax.inject.Inject

class ExodusDatabaseRepository @Inject constructor(
    exodusDatabase: ExodusDatabase
) {

    private val trackerDataDao = exodusDatabase.trackerDataDao()

    suspend fun saveTrackerData(trackerData: TrackerData) {
        return trackerDataDao.saveTrackerData(trackerData)
    }

    suspend fun getTrackers(id: Int): List<TrackerData> {
        return trackerDataDao.getTrackers(id)
    }

    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData> {
        return trackerDataDao.getTrackers(listOfID)
    }

    suspend fun updateTrackerData(trackerData: TrackerData) {
        return trackerDataDao.updateTrackerData(trackerData)
    }

    suspend fun deleteTrackerData(trackerData: TrackerData) {
        return trackerDataDao.deleteTrackerData(trackerData)
    }
}
