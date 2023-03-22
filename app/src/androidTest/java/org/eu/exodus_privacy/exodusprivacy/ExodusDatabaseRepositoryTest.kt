package org.eu.exodus_privacy.exodusprivacy

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabase
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.objects.Permission
import org.eu.exodus_privacy.exodusprivacy.objects.Source
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ExodusDatabaseRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @Inject
    lateinit var exodusDatabaseRepository: ExodusDatabaseRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoShouldCrash() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        val context = InstrumentationRegistry.getInstrumentation().context
        val assets = context.assets
        val bitmapStream = assets.open("mipmap/big_square_bigfs.png")
        val image = BitmapFactory.decodeStream(bitmapStream)

        val packageName = "com.test.testapp"
        val name = "TestApp"
        val versionName = "v1.0.0"
        val versionCode = 1L
        val permissions = emptyList<Permission>()
        val exodusVersionName = "TestApp"
        val exodusVersionCode = 1L
        val exodusTrackers = emptyList<Int>()
        val source = Source.GOOGLE
        val report = 0
        val created = ""
        val updated = ""

        // when
        val exodusAppEntry = ExodusApplication(
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

        // then
        exodusDatabaseRepository.saveApp(exodusAppEntry)

        val exception =
            try {
                exodusDatabaseRepository.getApp(packageName)
            } catch (
                exception: android.database.sqlite.SQLiteBlobTooBigException
            ) {
                exception
            }
        assertEquals(
            "android.database.sqlite.SQLiteBlobTooBigException: Row too big to fit into CursorWindow requiredPos=0, totalRows=1",
            exception.toString()
        )
    }
}