package org.eu.exodus_privacy.exodusprivacy.objects

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap

data class Application(
    val name: String = String(),
    val packageName: String = String(),
    val icon: Bitmap = Bitmap.createBitmap(48,48,Bitmap.Config.RGB_565),
    val versionName: String = String(),
    val versionCode: Long = 0L,
    val permissions: List<Permission> = emptyList(),
    val source: Source = Source.SYSTEM
)
