package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.res.ColorStateList
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.objects.VersionReport

fun Chip.setExodusColor(size: Int) {
    if (this.text != "?") {
        val colorRed = ContextCompat.getColor(context, R.color.colorRedLight)
        val colorYellow = ContextCompat.getColor(context, R.color.colorYellow)
        val colorGreen = ContextCompat.getColor(context, R.color.colorGreen)
        val colorDark = ContextCompat.getColor(context, R.color.textColorDark)
        val colorWhite = ContextCompat.getColor(context, R.color.textColorLikeWhite)

        val textColorStateList = when (size) {
            0 -> ColorStateList.valueOf(colorDark)
            in 1..4 -> ColorStateList.valueOf(colorDark)
            else -> ColorStateList.valueOf(colorWhite)
        }

        val backgroundColorStateList = when (size) {
            0 -> ColorStateList.valueOf(colorGreen)
            in 1..4 -> ColorStateList.valueOf(colorYellow)
            else -> ColorStateList.valueOf(colorRed)
        }

        this.chipIconTint = textColorStateList
        this.setTextColor(textColorStateList)
        this.chipBackgroundColor = backgroundColorStateList
    } else {
        val colorText = ContextCompat.getColor(context, R.color.colorPrimary)
        val colorBackground = ContextCompat.getColor(context, R.color.chipColor)

        this.chipIconTint = ColorStateList.valueOf(colorText)
        this.setTextColor(colorText)
        this.chipBackgroundColor = ColorStateList.valueOf(colorBackground)
    }
}

fun Chip.setVersionReport(app: ExodusApplication) {
    val versionReport = when (app.exodusVersionCode) {
        app.versionCode -> VersionReport.MATCH
        0L -> VersionReport.UNAVAILABLE
        else -> VersionReport.MISMATCH
    }
    chipIcon = ContextCompat.getDrawable(context, versionReport.iconIdRes)
    setOnClickListener {
        Toast.makeText(
            context,
            context.getString(
                versionReport.stringIdRes
            ),
            Toast.LENGTH_LONG
        ).show()
    }
}
