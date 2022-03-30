package org.eu.exodus_privacy.exodusprivacy.manager.database.app

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExodusApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveApp(exodusApplication: ExodusApplication)

    @Query("SELECT * FROM exodusapplication WHERE packageName=:packageName")
    suspend fun getApp(packageName: String): List<ExodusApplication>

    @Query("SELECT * FROM exodusapplication")
    fun getAllApps(): LiveData<List<ExodusApplication>>
}
