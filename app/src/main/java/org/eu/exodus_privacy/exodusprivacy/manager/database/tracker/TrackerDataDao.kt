package org.eu.exodus_privacy.exodusprivacy.manager.database.tracker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackerDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrackerData(trackerData: TrackerData)

    @Query("SELECT * FROM trackerdata WHERE id=:id")
    suspend fun getTrackers(id: Int): List<TrackerData>

    @Query("SELECT * FROM trackerdata WHERE presentOnDevice = 1")
    fun getAllTrackers(): LiveData<List<TrackerData>>

    @Query("SELECT * FROM trackerdata WHERE id IN (:listOfID) ORDER BY name")
    suspend fun getTrackers(listOfID: List<Int>): List<TrackerData>

    @Delete
    suspend fun deleteTrackerData(trackerData: TrackerData)
}
