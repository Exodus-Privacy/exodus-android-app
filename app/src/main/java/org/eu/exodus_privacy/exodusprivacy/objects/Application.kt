package org.eu.exodus_privacy.exodusprivacy.objects

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

data class Application(
    val name: String = String(),
    val packageName: String = String(),
    val icon: Drawable = ColorDrawable(Color.TRANSPARENT),
    val versionName: String = String(),
    val versionCode: Long = 0L
)
