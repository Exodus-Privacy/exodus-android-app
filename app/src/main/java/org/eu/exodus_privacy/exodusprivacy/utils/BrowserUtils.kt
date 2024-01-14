package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import java.util.Collections

fun startActivity(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        false
    }
}

fun openURL(customTabsIntent: CustomTabsIntent, context: Context, url: String) {
    val packageName = CustomTabsClient.getPackageName(
        context,
        Collections.emptyList(),
    )
    if (packageName != null) {
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } else {
        startActivity(context, url)
    }
}
