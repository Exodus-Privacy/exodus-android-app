package org.eu.exodus_privacy.exodusprivacy

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ExodusUpdateServiceTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    val testScope: TestScope = TestScope(testDispatcher)

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun serviceShouldBeStarted() {
        // given
        hiltRule.inject()

        val serviceIntent = Intent(
            context,
            ExodusUpdateService::class.java
        )
        serviceRule.startService(serviceIntent)

        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as ExodusUpdateService.LocalBinder).getService()

                // when
                val serviceRuns = service.serviceRuns()

                // then
                assert(serviceRuns)

                serviceRule.unbindService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
    }

    @Test
    fun countsAppsHavingTrackersCorrectly() {
        // given
        hiltRule.inject()

        val serviceIntent = Intent(
            context,
            ExodusUpdateService::class.java
        )
        serviceRule.startService(serviceIntent)

        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as ExodusUpdateService.LocalBinder).getService()

                // when
                val appsList = mutableListOf(
                    ExodusApplication(exodusTrackers = listOf(0)),
                    ExodusApplication(exodusTrackers = listOf(1)),
                    ExodusApplication(exodusTrackers = listOf())
                )

                // then
                assert(service.countAppsHavingTrackers(appsList) == 2)

                serviceRule.unbindService()
            }
            override fun onServiceDisconnected(name: ComponentName?) {}
        }
    }
}
