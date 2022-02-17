package org.eu.exodus_privacy.exodusprivacy.manager.network.data

data class Tracker(
    val categories: List<String> = emptyList(),
    val code_signature: String = String(),
    val creation_date: String = String(),
    val description: String = String(),
    val name: String = String(),
    val network_signature: String = String(),
    val website: String = String()
)