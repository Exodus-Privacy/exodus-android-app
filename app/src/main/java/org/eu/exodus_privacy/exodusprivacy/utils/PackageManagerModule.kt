package org.eu.exodus_privacy.exodusprivacy.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.objects.Application
import org.eu.exodus_privacy.exodusprivacy.objects.Permission
import org.eu.exodus_privacy.exodusprivacy.objects.Source
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PackageManagerModule {

    private const val GOOGLE_PLAY_STORE = "com.android.vending"
    private const val AURORA_STORE = "com.aurora.store"
    private const val FDROID = "org.fdroid.fdroid"
    private val SYSTEM: String? = null

    private val TAG = PackageManagerModule::class.java.simpleName

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
                val permsList = getPermissionList(appPerms, packageManager)
                val app = Application(
                    it.applicationInfo.loadLabel(packageManager).toString(),
                    it.packageName,
                    it.applicationInfo.loadIcon(packageManager).toBitmap(),
                    it.versionName ?: "",
                    PackageInfoCompat.getLongVersionCode(it),
                    permsList,
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
            SYSTEM -> Source.SYSTEM
            else -> Source.USER
        }
    }

    private fun validPackage(packageName: String, packageManager: PackageManager): Boolean {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return (
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 ||
                packageManager.getLaunchIntentForPackage(packageName) != null
            ) &&
            appInfo.enabled
    }

    private fun getPermissionList(
        permissionList: List<String>,
        packageManager: PackageManager
    ): List<Permission> {
        val permsList = mutableListOf<Permission>()
        permissionList.forEach { permissionName ->
            var permInfo: PermissionInfo? = null
            try {
                permInfo = packageManager.getPermissionInfo(
                    permissionName,
                    PackageManager.GET_META_DATA
                )
            } catch (exception: PackageManager.NameNotFoundException) {
                Log.d(TAG, "Unable to find info about $permissionName")
            }

            // Encapsulate regex modification
            val permissionString = permissionName.replace("[^>]*[a-z][.]".toRegex(), "")

            // Labels and desc can be null for undocumented permissions, filter them out
            permInfo?.loadLabel(packageManager)?.let { label ->
                permsList.add(
                    Permission(
                        permissionString,
                        label.toString(),
                        permInfo.loadDescription(packageManager)?.toString() ?: "",
                    )
                )
            } ?: run {
                permsList.add(
                    Permission(
                        permissionString,
                        permissionName,
                    )
                )
            }
        }
        permsList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.permission })
        return permsList
    }
}
