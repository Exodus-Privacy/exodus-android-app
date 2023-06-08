package org.eu.exodus_privacy.exodusprivacy.manager.database.app

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExodusApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(exodusApplication: ExodusApplication)

    @Query("SELECT * FROM exodusapplication WHERE packageName=:packageName")
    suspend fun queryApp(packageName: String): List<ExodusApplication>

    @Query("SELECT * FROM exodusapplication WHERE packageName IN (:listOfPackages) ORDER BY name COLLATE NOCASE")
    suspend fun queryApps(listOfPackages: List<String>): List<ExodusApplication>

    @Query("SELECT * FROM exodusapplication ORDER BY name COLLATE NOCASE")
    fun queryAllApps(): LiveData<List<ExodusApplication>>

    @Query("DELETE FROM exodusapplication WHERE packageName IN (:listOfPackages)")
    suspend fun deleteApp(listOfPackages: List<String>)

    @Query("SELECT packageName FROM exodusapplication")
    suspend fun getPackageNames(): List<String>
}
