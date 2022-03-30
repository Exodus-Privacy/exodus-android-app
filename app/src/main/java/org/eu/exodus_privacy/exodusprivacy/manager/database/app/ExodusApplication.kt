package org.eu.exodus_privacy.exodusprivacy.manager.database.app

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData

@Entity
data class ExodusApplication(
    @PrimaryKey val packageName: String = String(),
    val name: String = String(),
    val icon: Bitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(),
    val versionName: String = String(),
    val versionCode: Long = 0L,
    val permissions: List<String> = emptyList(),
    val exodusVersionName: String = String(),
    val exodusVersionCode: Long = 0L,
    val exodusTrackers: List<TrackerData> = emptyList()
)
