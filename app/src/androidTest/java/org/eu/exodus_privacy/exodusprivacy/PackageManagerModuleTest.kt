package org.eu.exodus_privacy.exodusprivacy

import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.utils.PackageManagerModule
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PackageManagerModuleTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    private val resolution = 96

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun iconsInAppListIsResolution() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        val serviceIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            ExodusUpdateService::class.java
        ).apply {
            action = ExodusUpdateService.START_SERVICE
        }

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        val service: ExodusUpdateService = (binder as ExodusUpdateService.LocalBinder).getService()

        // when
        val appList = service.applicationList

        // then
        appList.forEach {
            assert(it.icon.width == resolution && it.icon.height == resolution)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun packageInfoContainsPermissions() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val packagesWithPermissions = packages.filterNot { it.requestedPermissions == null }
        val packageManagerModule = PackageManagerModule

        // when
        val permissionsMap = packageManagerModule.generatePermissionsMap(packages, packageManager)

        // then
        val youtubePackage =
            packagesWithPermissions.filter { it.packageName == "com.google.android.youtube" }[0]
        val compareYoutubePackage = permissionsMap["com.google.android.youtube"]

        compareYoutubePackage?.forEach { perm ->
            assert(
                youtubePackage.requestedPermissions.any {
                    it.contains(perm.permission)
                }
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applicationListContainsPermissions() = runTest(testDispatcher) {
        // given
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManagerModule = PackageManagerModule
        val installedApps =
            context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val installedAppsWithPermissions =
            installedApps.filterNot { it.requestedPermissions == null }

        // when
        val appList = packageManagerModule.provideApplicationList(context)
        val appsWithPermissions = appList.filter { it.permissions.isNotEmpty() }

        // then
        installedAppsWithPermissions.forEach { pkg ->
            appsWithPermissions.forEach { app ->
                if (pkg.packageName == app.packageName) {
                    app.permissions.forEach {
                        assert(pkg.requestedPermissions.contains(it.permission))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun applicationInfoObjectsAreTheSame() = runTest(testDispatcher) {
        // given
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = context.packageManager
        val installedApps =
            context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS + PackageManager.GET_ACTIVITIES)
        var count = 0
        // when
        for (pkg in installedApps) {
            pkg.applicationInfo.name
            val appInfo = pkg.applicationInfo
            val comparePkgInfo = packageManager.getApplicationInfo(pkg.packageName, 0)
            val applicationInfoObjectsAreTheSame =
                appInfo.packageName == comparePkgInfo.packageName &&
                    appInfo.enabled == comparePkgInfo.enabled &&
                    appInfo.flags == comparePkgInfo.flags &&
                    appInfo.icon == comparePkgInfo.icon
            // then
            assert(applicationInfoObjectsAreTheSame)
            count += 1
        }
    }
}
