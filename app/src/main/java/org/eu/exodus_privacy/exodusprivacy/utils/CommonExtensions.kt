package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.objects.VersionReport
import java.util.Locale

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
        val colorForeground =
            ColorStateList.valueOf(ContextCompat.getColor(context, com.google.android.material.R.color.m3_chip_text_color))
        this.chipIconTint = colorForeground
        this.setTextColor(colorForeground)
        val colorBackground = ContextCompat.getColor(context, R.color.chipColor)
        this.chipBackgroundColor = ColorStateList.valueOf(colorBackground)
    }
}

fun Chip.setVersionReport(app: ExodusApplication) {
    val versionReport = when (app.exodusVersionCode) {
        0L -> VersionReport.UNAVAILABLE
        app.versionCode -> VersionReport.MATCH
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

fun getLanguage(): String {
    return Locale.getDefault().language
}

fun PackageManager.getInstalledPackagesList(flags: Int): List<PackageInfo> {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        @Suppress("DEPRECATION")
        this.getInstalledPackages(flags)
    } else {
        val newFlags = PackageManager.PackageInfoFlags.of(flags.toLong())
        this.getInstalledPackages(newFlags)
    }
}

fun PackageManager.getSource(packageName: String): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.getInstallSourceInfo(packageName).installingPackageName
    } else {
        @Suppress("DEPRECATION")
        this.getInstallerPackageName(packageName)
    }
}
