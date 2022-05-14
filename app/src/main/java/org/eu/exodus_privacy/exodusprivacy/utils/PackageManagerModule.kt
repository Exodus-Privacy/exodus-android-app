package org.eu.exodus_privacy.exodusprivacy.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
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
        val packageList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val applicationList = mutableListOf<Application>()

        packageList.forEach {
            if (validPackage(it.packageName, packageManager)) {
                val appPerms = it.requestedPermissions?.toList() ?: emptyList()
                val app = Application(
                    it.applicationInfo.loadLabel(packageManager).toString(),
                    it.packageName,
                    it.applicationInfo.loadIcon(packageManager).toBitmap(),
                    it.versionName,
                    PackageInfoCompat.getLongVersionCode(it),
                    appPerms
                )
                applicationList.add(app)
            }
        }
        applicationList.sortBy { it.name }
        return applicationList
    }

    private fun validPackage(packageName: String, packageManager: PackageManager): Boolean {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
            appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 ||
            packageManager.getLaunchIntentForPackage(packageName) != null
    }
}
