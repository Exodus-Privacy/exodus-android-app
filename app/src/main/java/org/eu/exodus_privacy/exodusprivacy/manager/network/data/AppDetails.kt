package org.eu.exodus_privacy.exodusprivacy.manager.network.data

data class AppDetails(
    val apk_hash: String = String(),
    val app_name: String = String(),
    val created: String = String(),
    val creator: String = String(),
    val downloads: String = String(),
    val handle: String = String(),
    val icon_hash: String = String(),
    val permissions: List<String> = emptyList(),
    val report: Int = 0,
    val source: String = String(),
    val trackers: List<Int> = emptyList(),
    val uaid: String = String(),
    val updated: String = String(),
    val version_code: String = String(),
    val version_name: String = String()
)
