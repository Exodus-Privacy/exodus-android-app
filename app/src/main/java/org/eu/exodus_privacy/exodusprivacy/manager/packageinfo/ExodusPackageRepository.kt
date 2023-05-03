package org.eu.exodus_privacy.exodusprivacy.manager.packageinfo

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.eu.exodus_privacy.exodusprivacy.objects.Application
import org.eu.exodus_privacy.exodusprivacy.objects.Permission
import org.eu.exodus_privacy.exodusprivacy.objects.Source
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import javax.inject.Inject

class ExodusPackageRepository @Inject constructor(
    val packageManager: PackageManager,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher
) {
    private val TAG = ExodusPackageRepository::class.java.simpleName
    private val GOOGLE_PLAY_STORE = "com.android.vending"
    private val AURORA_STORE = "com.aurora.store"
    private val FDROID = "org.fdroid.fdroid"
    private val SYSTEM: String? = null
    private val resolution = 96

    suspend fun getApplicationList(
        validPackages: List<PackageInfo>
    ): MutableList<Application> {
        val permissionsMap = generatePermissionsMap(validPackages, packageManager)
        val applicationList = mutableListOf<Application>()
        validPackages.forEach { packageInfo ->
            Log.d(TAG, "Found package: ${packageInfo.packageName}.")
            val app = Application(
                packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                packageInfo.packageName,
                packageInfo.applicationInfo.loadIcon(packageManager)
                    .toBitmap(resolution, resolution),
                packageInfo.versionName ?: "",
                PackageInfoCompat.getLongVersionCode(packageInfo),
                permissionsMap[packageInfo.packageName] ?: emptyList(),
                getAppStore(packageInfo.packageName, packageManager)
            )
            Log.d(TAG, "Add app: $app")
            applicationList.add(app)
        }
        applicationList.sortBy { it.name }
        return applicationList
    }

    fun getValidPackageList(): MutableList<PackageInfo> {
        val packageList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val validPackages = mutableListOf<PackageInfo>()
        packageList.forEach { pkgInfo ->
            if (validPackage(pkgInfo, packageManager)) {
                validPackages.add(pkgInfo)
            }
        }
        return validPackages
    }

    suspend fun generatePermissionsMap(
        packages: List<PackageInfo>,
        packageManager: PackageManager
    ): MutableMap<String, List<Permission>> {
        return withContext(ioDispatcher) {
            val packagesWithPermissions = packages.filterNot { it.requestedPermissions == null }
            Log.d(TAG, "Packages with perms: $packagesWithPermissions")
            val permissionInfoSet = packagesWithPermissions.fold(
                hashSetOf<String>()
            ) { acc, next ->
                if (next.requestedPermissions != null) {
                    acc.addAll(next.requestedPermissions)
                }
                acc
            }
            Log.d(TAG, "Permission Info Set: $permissionInfoSet")
            val permissionMap = hashMapOf<String, List<Permission>>()
            val permissionSet = permissionInfoSet.map { permissionName ->
                generatePermission(permissionName, packageManager)
            }
            Log.d(TAG, "Permission Set: $permissionSet")
            packagesWithPermissions.forEach { packageInfo ->
                permissionMap[packageInfo.packageName] = permissionSet.filter { perm ->
                    packageInfo.requestedPermissions.any { reqPerm ->
                        reqPerm == perm.longName
                    }
                }
            }
            return@withContext permissionMap
        }
    }

    private fun generatePermission(longName: String, packageManager: PackageManager): Permission {
        var permInfo: PermissionInfo? = null
        try {
            permInfo = packageManager.getPermissionInfo(
                longName,
                PackageManager.GET_META_DATA
            )
        } catch (exception: PackageManager.NameNotFoundException) {
            Log.d(TAG, "Unable to find info about $longName.")
        }
        val shortName = longName.split('.').last()
        permInfo?.loadLabel(packageManager)?.let { label ->
            return Permission(
                shortName,
                longName,
                label.toString(),
                permInfo.loadDescription(packageManager)?.toString() ?: ""
            )
        } ?: run {
            return Permission(
                shortName,
                longName,
                longName
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun getAppStore(packageName: String, packageManager: PackageManager): Source {
        val appStore = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } else {
            packageManager.getInstallerPackageName(packageName)
        }
        Log.d(TAG, "Found AppStore: $appStore for app: $packageName.")
        return when (appStore) {
            GOOGLE_PLAY_STORE -> Source.GOOGLE
            AURORA_STORE -> Source.GOOGLE
            FDROID -> Source.FDROID
            SYSTEM -> Source.SYSTEM
            else -> Source.USER
        }
    }

    private fun validPackage(packageInfo: PackageInfo, packageManager: PackageManager): Boolean {
        val appInfo = packageInfo.applicationInfo
        val packageName = packageInfo.packageName
        return (
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 ||
                packageManager.getLaunchIntentForPackage(packageName) != null
            ) &&
            appInfo.enabled
    }
}
