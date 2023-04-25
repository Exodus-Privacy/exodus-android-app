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
        val installedApps = context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val installedAppsWithPermissions = installedApps.filterNot { it.requestedPermissions == null }

        // when
        val appList = packageManagerModule.provideApplicationList(context)
        val appsWithPermissions = appList.filter { it.permissions.isNotEmpty() }

        // then
        installedAppsWithPermissions.forEach { pkg ->
            appsWithPermissions.forEach { app ->
                if (pkg.packageName == app.packageName) {
                    val reqPerm = mutableListOf<String>()
                    pkg.requestedPermissions.forEach { reqPerm.add(it) }
                    val compPerm = mutableListOf<String>()
                    app.permissions.forEach { compPerm.add(it.permission) }
                    assert(reqPerm.size == compPerm.size)
                }
            }
        }
    }
}
