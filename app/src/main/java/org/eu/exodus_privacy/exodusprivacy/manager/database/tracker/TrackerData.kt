package org.eu.exodus_privacy.exodusprivacy.manager.database.tracker

import androidx.room.Entity
import androidx.room.PrimaryKey

// Copy of Tracker data class with id to avoid conflicts
@Entity
data class TrackerData(
    @PrimaryKey val id: Int,
    val categories: List<String>,
    val code_signature: String,
    val creation_date: String,
    val description: String,
    val name: String,
    val network_signature: String,
    val website: String
)
