package org.eu.exodus_privacy.exodusprivacy.manager.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplicationDao
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerDataDao

@Database(entities = [TrackerData::class, ExodusApplication::class], version = 1, exportSchema = false)
@TypeConverters(ExodusDatabaseConverters::class)
abstract class ExodusDatabase : RoomDatabase() {

    abstract fun trackerDataDao(): TrackerDataDao

    abstract fun exodusApplicationDao(): ExodusApplicationDao
}
