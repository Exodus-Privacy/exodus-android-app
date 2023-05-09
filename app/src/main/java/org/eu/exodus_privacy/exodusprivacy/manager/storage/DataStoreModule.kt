package org.eu.exodus_privacy.exodusprivacy.manager.storage

import androidx.datastore.preferences.core.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

    @Provides
    fun providesGson(): Gson {
        return Gson()
    }

    @Provides
    fun providesPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("ExodusSettings")
    }

    @Provides
    fun providesTypeToken(): TypeToken<Map<String, ExodusConfig>> {
        return object : TypeToken<Map<String, ExodusConfig>>() {}
    }
}

