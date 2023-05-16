package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.manager.storage.DataStoreName
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusConfig
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusDataStoreRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/* Copyright 2019 Google LLC.
SPDX-License-Identifier: Apache-2.0 */
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

@HiltAndroidTest
class ExodusDataStoreRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var dataStoreRepository: ExodusDataStoreRepository<ExodusConfig>

    @Before
    fun setup() {
        context = getInstrumentation().targetContext
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testReturnsDefaults() = runTest {
        // given
        dataStoreRepository = ExodusDataStoreRepository(
            Gson(),
            stringPreferencesKey("testKey"),
            object : TypeToken<Map<String, ExodusConfig>>() {},
            DataStoreName("testDataStore1"),
            context
        )

        // when
        val defaults = dataStoreRepository.getAll().first()

        // then
        assert(defaults.containsValue(ExodusConfig("privacy_policy_consent", false)))
        assert(defaults.containsValue(ExodusConfig("is_setup_complete", false)))
        assert(defaults.containsValue(ExodusConfig("notification_requested", false)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testInsertsAndRetrievesCorrectVal() = runTest {
        // given
        dataStoreRepository = ExodusDataStoreRepository(
            Gson(),
            stringPreferencesKey("testKey"),
            object : TypeToken<Map<String, ExodusConfig>>() {},
            DataStoreName("testDataStore2"),
            context
        )

        val newValues = mapOf(
            "privacy_policy" to ExodusConfig("privacy_policy_consent", true),
            "app_setup" to ExodusConfig("is_setup_complete", true),
            "notification_perm" to ExodusConfig("notification_requested", true)
        )

        // when
        dataStoreRepository.insert(newValues)
        val values = dataStoreRepository.getAll().first()

        // then
        assert(values.containsValue(ExodusConfig("privacy_policy_consent", true)))
        assert(values.containsValue(ExodusConfig("is_setup_complete", true)))
        assert(values.containsValue(ExodusConfig("notification_requested", true)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testInsertsAppSetupCOrrectly() = runTest {
        // given
        dataStoreRepository = ExodusDataStoreRepository(
            Gson(),
            stringPreferencesKey("testKey"),
            object : TypeToken<Map<String, ExodusConfig>>() {},
            DataStoreName("testDataStore3"),
            context
        )

        val values = mapOf(
            "privacy_policy" to ExodusConfig("privacy_policy_consent", true),
            "app_setup" to ExodusConfig("is_setup_complete", true),
            "notification_perm" to ExodusConfig("notification_requested", true)
        )

        // when
        dataStoreRepository.insert(values)
        dataStoreRepository.insertAppSetup(ExodusConfig("is_setup_complete", false))
        val appSetup = dataStoreRepository.get("app_setup").first()

        // then
        assert(appSetup == ExodusConfig("is_setup_complete", false))
    }
}
