package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.manager.storage.DataStoreName
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusConfig
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusDataStoreRepository
import org.junit.Rule
import kotlin.test.BeforeTest
import kotlin.test.Test

@HiltAndroidTest
class ExodusDataStoreRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var dataStoreRepository: ExodusDataStoreRepository<ExodusConfig>

    @BeforeTest
    fun setup() {
        context = getInstrumentation().targetContext
    }

    @Test
    fun testReturnsDefaults() = runTest {
        // given
        dataStoreRepository = ExodusDataStoreRepository(
            Gson(),
            stringPreferencesKey("testKey"),
            object : TypeToken<Map<String, ExodusConfig>>() {},
            DataStoreName("testDataStore1"),
            context,
        )

        // when
        val defaults = dataStoreRepository.getAll().first()

        // then
        assert(defaults.containsValue(ExodusConfig("privacy_policy_consent", false)))
        assert(defaults.containsValue(ExodusConfig("notification_requested", false)))
    }

    @Test
    fun testInsertsAndRetrievesCorrectVal() = runTest {
        // given
        dataStoreRepository = ExodusDataStoreRepository(
            Gson(),
            stringPreferencesKey("testKey"),
            object : TypeToken<Map<String, ExodusConfig>>() {},
            DataStoreName("testDataStore2"),
            context,
        )

        val newValues = mapOf(
            "privacy_policy" to ExodusConfig("privacy_policy_consent", true),
            "notification_perm" to ExodusConfig("notification_requested", true),
        )

        // when
        dataStoreRepository.insert(newValues)
        val values = dataStoreRepository.getAll().first()

        // then
        assert(values.containsValue(ExodusConfig("privacy_policy_consent", true)))
        assert(values.containsValue(ExodusConfig("notification_requested", true)))
    }
}
