package org.eu.exodus_privacy.exodusprivacy.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.objects.Application
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PackageManagerModule {

    @Singleton
    @Provides
    @SuppressLint("QueryPermissionsNeeded")
    fun provideApplicationList(@ApplicationContext context: Context): MutableList<Application> {
        val packageManager = context.packageManager
        val packageList = packageManager.getInstalledPackages(0)
        val applicationList = mutableListOf<Application>()

        packageList.forEach {
            val app = Application(
                it.applicationInfo.loadLabel(packageManager).toString(),
                it.packageName,
                it.applicationInfo.loadIcon(packageManager),
                it.versionName,
                PackageInfoCompat.getLongVersionCode(it)
            )
            applicationList.add(app)
        }
        return applicationList
    }
}