package org.eu.exodus_privacy.exodusprivacy.manager.database.tracker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TrackerDataDao {

    @Insert
    suspend fun saveTrackerData(trackerData: TrackerData)

    @Query("SELECT * FROM trackerdata WHERE id=:id")
    suspend fun getTrackers(id: Int): List<TrackerData>

    @Query("SELECT * FROM trackerdata WHERE id IN (:listOfID)")
    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData>

    @Update
    suspend fun updateTrackerData(trackerData: TrackerData)

    @Delete
    suspend fun deleteTrackerData(trackerData: TrackerData)
}
