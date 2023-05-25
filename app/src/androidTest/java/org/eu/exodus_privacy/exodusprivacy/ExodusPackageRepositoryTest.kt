package org.eu.exodus_privacy.exodusprivacy

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.manager.packageinfo.ExodusPackageRepository
import org.eu.exodus_privacy.exodusprivacy.utils.getInstalledPackagesList
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ExodusPackageRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    private val testDispatcher = StandardTestDispatcher()

    @Inject
    lateinit var exodusPackageRepository: ExodusPackageRepository

    private val resolution = 96

    @Test
    fun iconsInAppListIsResolution() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        // when
        val valid = exodusPackageRepository.getValidPackageList()
        val appList = exodusPackageRepository.getApplicationList(valid)

        // then
        appList.forEach {
            assert(it.icon.width == resolution && it.icon.height == resolution)
        }
    }

    @Test
    fun packageInfoContainsPermissions() = runTest(testDispatcher) {
        // given
        hiltRule.inject()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackagesList(PackageManager.GET_PERMISSIONS)
        val packagesWithPermissions = packages.filterNot { it.requestedPermissions == null }

        // when
        val permissionsMap =
            exodusPackageRepository.generatePermissionsMap(packages, packageManager)

        // then
        val youtubePackage =
            packagesWithPermissions.filter { it.packageName == "com.google.android.youtube" }[0]
        val compareYoutubePackage = permissionsMap["com.google.android.youtube"]

        compareYoutubePackage?.forEach { perm ->
            assert(
                youtubePackage.requestedPermissions.any {
                    it.contains(perm.longName)
                }
            )
        }
    }

    @Test
    fun applicationListContainsPermissions() = runTest(testDispatcher) {
        // given
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val installedApps =
            context.packageManager.getInstalledPackagesList(PackageManager.GET_PERMISSIONS)
        val installedAppsWithPermissions =
            installedApps.filterNot { it.requestedPermissions == null }

        // when
        val valid = exodusPackageRepository.getValidPackageList()
        val appList = exodusPackageRepository.getApplicationList(valid)
        val appsWithPermissions = appList.filter { it.permissions.isNotEmpty() }

        // then
        installedAppsWithPermissions.forEach { pkg ->
            appsWithPermissions.forEach { app ->
                if (pkg.packageName == app.packageName) {
                    app.permissions.forEach {
                        assert(pkg.requestedPermissions.contains(it.longName))
                    }
                }
            }
        }
    }

    @Test
    fun applicationInfoObjectsAreTheSame() = runTest(testDispatcher) {
        // given
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = context.packageManager
        val installedApps =
            context.packageManager.getInstalledPackagesList(
                PackageManager.GET_PERMISSIONS + PackageManager.GET_ACTIVITIES
            )
        var count = 0

        // when
        var comparePkgInfo: ApplicationInfo
        for (pkg in installedApps) {
            pkg.applicationInfo.name
            val appInfo = pkg.applicationInfo
            comparePkgInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(
                    pkg.packageName,
                    0
                )
            } else {
                packageManager.getApplicationInfo(
                    pkg.packageName,
                    PackageManager.ApplicationInfoFlags.of(0.toLong())
                )
            }
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
