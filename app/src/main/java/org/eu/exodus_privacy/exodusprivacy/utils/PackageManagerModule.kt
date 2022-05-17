package org.eu.exodus_privacy.exodusprivacy.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.objects.Application
import org.eu.exodus_privacy.exodusprivacy.objects.Source
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PackageManagerModule {

    private const val GOOGLE_PLAY_STORE = "com.android.vending"
    private const val AURORA_STORE = "com.aurora.store"
    private const val FDROID = "org.fdroid.fdroid"
	private const val USER_INSTALL = "com.google.android.packageinstaller"

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
                    appPerms,
                    getAppStore(it.packageName, packageManager)
                )
                applicationList.add(app)
            }
        }
        applicationList.sortBy { it.name }
        return applicationList
    }

    @Suppress("DEPRECATION")
    private fun getAppStore(packageName: String, packageManager: PackageManager): Source {
        val appStore = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } else {
            packageManager.getInstallerPackageName(packageName)
        }
        return when (appStore) {
            GOOGLE_PLAY_STORE -> Source.GOOGLE
            AURORA_STORE -> Source.GOOGLE
            FDROID -> Source.FDROID
			USER_INSTALL -> Source.USER
            else -> Source.UNKNOWN
        }
    }

    private fun validPackage(packageName: String, packageManager: PackageManager): Boolean {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
            appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 ||
            packageManager.getLaunchIntentForPackage(packageName) != null
    }
}
