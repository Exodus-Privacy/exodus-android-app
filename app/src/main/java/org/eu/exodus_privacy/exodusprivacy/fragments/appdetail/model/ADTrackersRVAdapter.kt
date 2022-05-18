package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewTrackerItemBinding
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData

class ADTrackersRVAdapter :
    ListAdapter<TrackerData, ADTrackersRVAdapter.ViewHolder>(ADTrackersDiffUtil()) {

    inner class ViewHolder(val binding: RecyclerViewTrackerItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerViewTrackerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val app = getItem(position)

        holder.binding.apply {
            trackerTitleTV.text = app.name
            app.categories.forEach {
                val chip = Chip(context)
                val chipStyle = ChipDrawable.createFromAttributes(
                    context,
                    null,
                    0,
                    R.style.Theme_Exodus_Chip
                )
                chip.text = it
                chip.setChipDrawable(chipStyle)
                chipGroup.addView(chip)
            }
        }
    }
}
