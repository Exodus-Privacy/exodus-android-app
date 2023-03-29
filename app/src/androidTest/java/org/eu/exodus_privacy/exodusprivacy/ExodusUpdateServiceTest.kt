package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.utils.DispatcherModule
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatcherModule::class]
)
object FakeDispatcherModule {
    @OptIn(ExperimentalCoroutinesApi::class)
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher =
        StandardTestDispatcher(TestCoroutineScheduler())
}

@HiltAndroidTest
class ExodusUpdateServiceTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    private val testDispatcher: CoroutineDispatcher =
        FakeDispatcherModule.providesIoDispatcher()

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
