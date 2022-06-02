package org.eu.exodus_privacy.exodusprivacy.objects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.eu.exodus_privacy.exodusprivacy.R

enum class VersionReport(
    @DrawableRes val iconIdRes: Int,
    @StringRes val stringIdRes: Int
) {
    MATCH(R.drawable.ic_match, R.string.version_equals),
    UNAVAILABLE(R.drawable.ic_unavailable, R.string.analyzed),
    MISMATCH(R.drawable.ic_mismatch, R.string.version_mismatch);
}