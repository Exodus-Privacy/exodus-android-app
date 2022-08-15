package org.eu.exodus_privacy.exodusprivacy

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.eu.exodus_privacy.exodusprivacy.utils.PackageManagerModule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaseTest {

    /**
     * Test only to show current app permissions
     */
    @Test
    fun getAllPermissions() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        println(PackageManagerModule.provideApplicationList(appContext))
    }
}