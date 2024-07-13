package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.Context
import android.content.res.Configuration

fun getColumnScreen(context: Context): Int {
    return if (context.resources.configuration.smallestScreenWidthDp >= 600 && context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
}
