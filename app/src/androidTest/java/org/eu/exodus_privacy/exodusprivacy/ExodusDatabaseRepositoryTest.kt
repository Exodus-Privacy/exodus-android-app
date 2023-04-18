package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabase
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.objects.Permission
import org.eu.exodus_privacy.exodusprivacy.objects.Source
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.InputStream

@HiltAndroidTest
class ExodusDatabaseRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var exodusAppEntry: ExodusApplication
    private lateinit var exodusAppEntry2: ExodusApplication
    private lateinit var exodusTrackerDataEntry: TrackerData
    private lateinit var exodusTrackerDataEntry2: TrackerData
    private lateinit var context: Context
    private lateinit var assets: AssetManager
    private lateinit var bitmapStream: InputStream
    private lateinit var image: Bitmap
    private lateinit var testDB: ExodusDatabase
    private lateinit var exodusDatabaseRepository: ExodusDatabaseRepository

    private val packageName = "com.test.testapp"
    private val packageName2 = "com.test.testapp2"
    private val name = "TestApp"
    private val name2 = "TestApp2"
    private val versionName = "v1.0.0"
    private val versionCode = 1L
    private val permissions = emptyList<Permission>()
    private val exodusVersionName = "TestApp"
    private val exodusVersionCode = 1L
    private val exodusTrackers = emptyList<Int>()
    private val source = Source.GOOGLE
    private val report = 0
    private val created = ""
    private val updated = ""

    private val resolution = 144

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
        assets = context.assets
        bitmapStream = assets.open("mipmap/big_square_bigfs.png")
        image = BitmapFactory.decodeStream(bitmapStream)

        testDB = Room.inMemoryDatabaseBuilder(
            context,
            ExodusDatabase::class.java
        ).build()

        exodusDatabaseRepository = ExodusDatabaseRepository(
            testDB,
            testDispatcher
        )

        // when
        exodusAppEntry = ExodusApplication(
            packageName,
            name,
            image,
            versionName,
            versionCode,
            permissions,
            exodusVersionName,
            exodusVersionCode,
            exodusTrackers,
            source,
            report,
            created,
            updated
        )

        exodusAppEntry2 = ExodusApplication(
            packageName2,
            name2,
            image.scale(resolution, resolution),
            versionName,
            versionCode,
            permissions,
            exodusVersionName,
            exodusVersionCode,
            exodusTrackers,
            source,
            report,
            created,
            updated
        )

        exodusTrackerDataEntry = TrackerData(
            id = 0,
            categories = emptyList(),
            code_signature = "",
            creation_date = "01.01.1970",
            description = "Lorem Ipsum",
            name = "TestTracker",
            network_signature = "Unknown",
            website = "example.com"
        )
    }

    @After
    fun teardown() {
        testDB.clearAllTables()
        testDB.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoShouldCrash() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        // then
        exodusDatabaseRepository.saveApp(exodusAppEntry)
        val exceptions = arrayListOf<String>()
        val dataBaseExceptionMessage = "android.database.sqlite.SQLiteBlobTooBigException: Row too big to fit into CursorWindow"
        val javaIllegalStateExceptionMessage = "java.lang.IllegalStateException: Couldn't read row"

        try {
            exodusDatabaseRepository.getApp(packageName)
        } catch (
            exception: java.lang.Exception
        ) { exceptions.add(exception.toString()) }
        assert(
            dataBaseExceptionMessage in exceptions[0] ||
                javaIllegalStateExceptionMessage in exceptions[0]
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoReturnsApp() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        exodusAppEntry = ExodusApplication(
            packageName,
            name,
            image.scale(resolution, resolution),
            versionName,
            versionCode,
            permissions,
            exodusVersionName,
            exodusVersionCode,
            exodusTrackers,
            source,
            report,
            created,
            updated
        )

        // then
        exodusDatabaseRepository.saveApp(exodusAppEntry)
        val retrievedApp = exodusDatabaseRepository.getApp(packageName)

        assert(retrievedApp.icon.sameAs(image.scale(resolution, resolution)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoReturnsApps() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        exodusAppEntry = ExodusApplication(
            packageName,
            name,
            image.scale(resolution, resolution),
            versionName,
            versionCode,
            permissions,
            exodusVersionName,
            exodusVersionCode,
            exodusTrackers,
            source,
            report,
            created,
            updated
        )

        // then
        exodusDatabaseRepository.saveApp(exodusAppEntry)
        exodusDatabaseRepository.saveApp(exodusAppEntry2)
        val retrievedApp = exodusDatabaseRepository.getApps(
            listOf(packageName, packageName2)
        )

        assert(retrievedApp.isNotEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoReturnsTracker() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        // then
        exodusDatabaseRepository.saveTrackerData(exodusTrackerDataEntry)
        val retrievedTracker = exodusDatabaseRepository.getTracker(0)

        assert(retrievedTracker.name == "TestTracker")
    }
}
