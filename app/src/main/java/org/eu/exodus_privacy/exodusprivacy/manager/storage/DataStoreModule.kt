package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreModule @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val preferenceDataStoreName = "exodusPreferences"
    private val Context.dataStore by preferencesDataStore(preferenceDataStoreName)

    private val PRIV_AGREE = booleanPreferencesKey("policyAgreement")
    val policyAgreement = context.dataStore.data.map { it[PRIV_AGREE] ?: false }

    private val APP_SETUP = booleanPreferencesKey("appSetup")
    val appSetup = context.dataStore.data.map { it[APP_SETUP] ?: false }

    private val NOTIFICATION_PERMS = stringSetPreferencesKey("notificationPerms")
    val notificationPerms = context.dataStore.data.map {
        it[NOTIFICATION_PERMS] ?: setOf("not_granted", "not_requested")
    }

    suspend fun saveNotificationPerms(status: Set<String>) {
        context.dataStore.edit {
            it[NOTIFICATION_PERMS] = status
        }
    }

    suspend fun savePolicyAgreement(status: Boolean) {
        context.dataStore.edit {
            it[PRIV_AGREE] = status
        }
    }

    suspend fun saveAppSetup(status: Boolean) {
        context.dataStore.edit {
            it[APP_SETUP] = status
        }
    }
}
