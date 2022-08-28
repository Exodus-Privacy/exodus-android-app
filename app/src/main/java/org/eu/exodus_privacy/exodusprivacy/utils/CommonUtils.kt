package org.eu.exodus_privacy.exodusprivacy.utils

import androidx.browser.customtabs.CustomTabsIntent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonUtils {

    @Singleton
    @Provides
    fun provideCustomTabsIntent(): CustomTabsIntent {
        return CustomTabsIntent.Builder().build()
    }
}
