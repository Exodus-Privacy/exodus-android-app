package org.eu.exodus_privacy.exodusprivacy.manager.network.data

import org.eu.exodus_privacy.exodusprivacy.objects.Source

/**
 * Picks the Exodus report ([AppDetails]) that best matches an installed app.
 *
 * The Exodus API returns one [AppDetails] per analysed variant of a package, and different
 * app stores can publish the same package name with different trackers (e.g. a Google Play
 * build bundling trackers that the F-Droid build does not). Selecting a report by version code
 * alone can therefore surface the wrong store's trackers for the installed app.
 *
 * Selection precedence:
 *  1. Same source **and** same version code as the installed app.
 *  2. Same source, highest version code.
 *  3. Same version code, any source.
 *  4. Highest version code overall (falls back to an empty [AppDetails] when the list is empty).
 *
 * The source of an installed app is only known for store installs ([Source.GOOGLE],
 * [Source.FDROID]); for other sources it is ignored and selection falls through to the
 * version-code based steps, preserving the previous behaviour.
 */
internal fun selectAppDetails(
    appDetailList: List<AppDetails>,
    versionCode: Long,
    source: Source,
): AppDetails {
    val apiSource = source.toApiSourceOrNull()
    if (apiSource != null) {
        appDetailList
            .firstOrNull { it.source == apiSource && it.version_code.toLongOrZero() == versionCode }
            ?.let { return it }
        appDetailList
            .filter { it.source == apiSource }
            .maxByOrNull { it.version_code.toLongOrZero() }
            ?.let { return it }
    }
    appDetailList
        .firstOrNull { it.version_code.toLongOrZero() == versionCode }
        ?.let { return it }
    return appDetailList.maxByOrNull { it.version_code.toLongOrZero() } ?: AppDetails()
}

/**
 * Maps the installed-app [Source] to the `source` string used by the Exodus API, or `null` when
 * the app was not installed from a store the API distinguishes.
 */
private fun Source.toApiSourceOrNull(): String? =
    when (this) {
        Source.GOOGLE -> "google"
        Source.FDROID -> "fdroid"
        Source.USER, Source.SYSTEM -> null
    }

private fun String.toLongOrZero(): Long = toLongOrNull() ?: 0L
