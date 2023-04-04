package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val testScope: TestScope = TestScope(testDispatcher)

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun serviceShouldBeStarted() = testScope.runTest {
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

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        val service: ExodusUpdateService = (binder as ExodusUpdateService.LocalBinder).getService()
        serviceRule.startService(serviceIntent)
        val serviceRuns = service.serviceRuns()

        assert(serviceRuns)
    }
}
