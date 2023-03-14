package org.eu.exodus_privacy.exodusprivacy

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.junit.Rule
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class ExodusUpdateServiceTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var exodusAPIRepository: ExodusAPIRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun exodusAPIRepoShouldTimeOut() = runTest {
        // given
        hiltRule.inject()

    }
}