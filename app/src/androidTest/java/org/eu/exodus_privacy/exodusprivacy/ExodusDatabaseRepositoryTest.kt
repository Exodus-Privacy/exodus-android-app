package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Output
import androidx.core.graphics.drawable.toIcon
import androidx.core.graphics.scale
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.manipulation.Ordering
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
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

    private lateinit var exodusAppEntry : ExodusApplication
    private lateinit var context : Context
    private lateinit var assets : AssetManager
    private lateinit var bitmapStream : InputStream
    private lateinit var image : Bitmap

    private val packageName = "com.test.testapp"
    private val name = "TestApp"
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

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
        assets = context.assets
        bitmapStream = assets.open("mipmap/big_square_bigfs.png")
        image = BitmapFactory.decodeStream(bitmapStream)

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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoShouldCrash() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusDatabaseRepoReturnsImage() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        // then
        val newImage : Bitmap
        if (image.width > 50) {
            newImage = image.scale(50, 50)
            exodusAppEntry = ExodusApplication(
                packageName,
                name,
                newImage,
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
        } else {
            newImage = image
        }

        exodusDatabaseRepository.saveApp(exodusAppEntry)
        val retrievedApp = exodusDatabaseRepository.getApp(packageName)

        assert( retrievedApp.icon.sameAs(newImage) )
    }
}