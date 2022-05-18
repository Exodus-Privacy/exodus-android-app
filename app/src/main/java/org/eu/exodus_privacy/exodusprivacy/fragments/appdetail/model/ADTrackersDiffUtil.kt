package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import androidx.recyclerview.widget.DiffUtil
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData

class ADTrackersDiffUtil : DiffUtil.ItemCallback<TrackerData>() {
    override fun areItemsTheSame(oldItem: TrackerData, newItem: TrackerData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackerData, newItem: TrackerData): Boolean {
        return when {
            oldItem.id != newItem.id -> false
            oldItem.categories != newItem.categories -> false
            oldItem.code_signature != newItem.code_signature -> false
            oldItem.creation_date != newItem.creation_date -> false
            oldItem.description != newItem.description -> false
            oldItem.name != newItem.name -> false
            oldItem.network_signature != newItem.network_signature -> false
            oldItem.website != newItem.website -> false
            else -> true
        }
    }
}
