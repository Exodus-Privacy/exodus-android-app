package org.eu.exodus_privacy.exodusprivacy.objects

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.Tracker

data class Application(
    val name: String = String(),
    val packageName: String = String(),
    val icon: Drawable = ColorDrawable(Color.TRANSPARENT),
    val versionName: String = String(),
    val versionCode: Long = 0L,
    val trackers: List<Tracker> = emptyList(),
    val permissions: List<String> = emptyList()
)
