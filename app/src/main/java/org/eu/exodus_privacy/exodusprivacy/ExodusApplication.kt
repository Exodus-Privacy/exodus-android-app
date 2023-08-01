package org.eu.exodus_privacy.exodusprivacy

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExodusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO:
        //  Get checked item value from preferences and set it below instead of 0
        val themeCheckedItem = 0
        when (themeCheckedItem) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
