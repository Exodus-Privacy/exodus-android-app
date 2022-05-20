package org.eu.exodus_privacy.exodusprivacy.manager.database.tracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackerData(
    @PrimaryKey val id: Int = 0,
    val categories: List<String> = emptyList(),
    val code_signature: String = String(),
    val creation_date: String = String(),
    val description: String = String(),
    val name: String = String(),
    val network_signature: String = String(),
    val website: String = String(),
    var presentOnDevice: Boolean = false,
    val exodusApplications: MutableList<String> = mutableListOf()
)
