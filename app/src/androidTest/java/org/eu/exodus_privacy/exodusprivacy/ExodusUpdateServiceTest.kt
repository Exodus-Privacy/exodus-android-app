package org.eu.exodus_privacy.exodusprivacy

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
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

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun serviceShouldBeStarted() = runTest(testDispatcher) {
        hiltRule.inject()

        val serviceIntent = Intent(
            context,
            ExodusUpdateService::class.java
        ).apply {
            action = ExodusUpdateService.START_SERVICE
        }.setClassName(
            "org.eu.exodus_privacy.exodusprivacy",
            "org.eu.exodus_privacy.exodusprivacy.ExodusUpdateService"
        )

        serviceRule.startService(serviceIntent)
        val serviceRuns = ActivityManager.RunningServiceInfo.FLAG_STARTED

        assert(serviceRuns == 1)

    }
}
