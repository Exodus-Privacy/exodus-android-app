package org.eu.exodus_privacy.exodusprivacy

import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabase
import org.junit.Rule
import java.io.IOException
import kotlin.test.Test

class FakeAutoMigrationSpec : AutoMigrationSpec {
    @Override
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
    }
}

@HiltAndroidTest
class DatabaseMigrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExodusDatabase::class.java,
        listOf(FakeAutoMigrationSpec()),
        FrameworkSQLiteOpenHelperFactory(),
    )

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE TrackerData ADD COLUMN totalNumberOfAppsHavingTrackers INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).apply {
            // db has schema version 1. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            execSQL(
                "INSERT INTO ExodusApplication (" +
                    "packageName, name, icon, versionName, versionCode," +
                    "permissions, exodusVersionName, exodusVersionCode, exodusTrackers," +
                    "source, report, created, updated) " +
                    "VALUES( " +
                    "'TestApp', 'TestApp', 'bitmap', 'TestApp', 'long', 'perms', " +
                    "'TestApp', 'long', 'tracks', 'source', 0, 'somewhen', 'somewhen' );",
            )
            execSQL(
                "INSERT INTO TrackerData (" +
                    "id, categories, code_signature, creation_date, description," +
                    "name, network_signature, website, presentOnDevice," +
                    "exodusApplications) " +
                    "VALUES(0, 'sdf', 'signature', 'date', 'description', 'TestTracker', 'signature'" +
                    ", 'webaddress.com', 'notTrue', 'mutableStringList');",
            )
            // Prepare for the next version.
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    }
}
