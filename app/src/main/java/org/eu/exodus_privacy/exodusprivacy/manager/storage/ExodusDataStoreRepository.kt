package org.eu.exodus_privacy.exodusprivacy.manager.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val OPERATION_SUCCESS = 1

data class ExodusConfig(val type: String, var enable: Boolean)

@Singleton
class ExodusDataStoreRepository<ExodusConfig> @Inject constructor(
    private val gson: Gson,
    private val preferenceKey: Preferences.Key<String>,
    private val typeToken: TypeToken<Map<String, ExodusConfig>>,
    private val name: DataStoreName,
    @ApplicationContext val context: Context
) : ExodusStorage<ExodusConfig> {

    private val Context.dataStore by preferencesDataStore(name.name)
    private val dataStore = context.dataStore

    private fun defaults(): String {
        return gson.toJson( mapOf(
            "privacy_policy" to ExodusConfig("privacy_policy_consent", false),
            "app_setup" to ExodusConfig("is_setup_complete", false),
            "notification_perm" to ExodusConfig("notification_requested", false)
        ))
    }

    override fun getAll(): Flow<Map<String, ExodusConfig>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[preferenceKey] ?: defaults()
            val elements = gson.fromJson(jsonString, typeToken)
            elements
        }
    }

    override fun get(key: String): Flow<ExodusConfig> {
        return getAll().map { cachedData ->
            cachedData[key]!!
        }
    }

    override suspend fun insert(data: Map<String, ExodusConfig>) {
        dataStore.edit {
            val jsonString = gson.toJson(data, typeToken.type)
            it[preferenceKey] = jsonString
        }
    }

    override suspend fun insertAppSetup(data: ExodusConfig) {
        val currentData = getAll().first() as MutableMap
        currentData["app_setup"] = data
        dataStore.edit {
            val jsonString = gson.toJson(currentData, typeToken.type)
            it[preferenceKey] = jsonString
        }

    }

    override fun clearAll(): Flow<Int> {
        return flow {
            dataStore.edit {
                it.remove(preferenceKey)
                emit(OPERATION_SUCCESS)
            }
        }
    }

}