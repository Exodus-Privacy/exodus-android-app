package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.ClipData.newPlainText
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import org.eu.exodus_privacy.exodusprivacy.R
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
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    com.google.android.material.R.color.m3_chip_text_color,
                ),
            )
        this.chipIconTint = colorForeground
        this.setTextColor(colorForeground)
        val colorBackground = ContextCompat.getColor(context, R.color.chipColor)
        this.chipBackgroundColor = ColorStateList.valueOf(colorBackground)
    }
}

fun getLanguage(): String {
    return Locale.getDefault().language
}

fun PackageManager.getInstalledPackagesList(flags: Int): List<PackageInfo> {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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

fun copyToClipboard(context: Context, string: String): Boolean {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(newPlainText("", string))
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }
    return true
}
