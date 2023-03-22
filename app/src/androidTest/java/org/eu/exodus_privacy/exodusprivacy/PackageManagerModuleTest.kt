package org.eu.exodus_privacy.exodusprivacy

import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PackageManagerModuleTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun iconsInAppListNotGreater50Px() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        val serviceIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            ExodusUpdateService::class.java
        ).apply {
            action = ExodusUpdateService.START_SERVICE
        }

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        val service: ExodusUpdateService = (binder as ExodusUpdateService.LocalBinder).getService()

        // when
        val appList = service.applicationList

        // then

        appList.forEach {
            assert( it.icon.width <= 50 && it.icon.height <= 50 )
        }
    }
}