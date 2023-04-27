package org.eu.exodus_privacy.exodusprivacy.manager.packageinfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PackageManagerModule {
    @Singleton
    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }
}
