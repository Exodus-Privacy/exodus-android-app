package org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewTrackerItemBinding
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData

class TrackersRVAdapter(private val showSuggestions: Boolean) :
    ListAdapter<TrackerData, TrackersRVAdapter.ViewHolder>(TrackersDiffUtil()) {

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
        val trackerApps = mutableSetOf<String>().apply {
            currentList.forEach { this.addAll(it.exodusApplications) }
        }
        val app = getItem(position)

        // Fix padding for TrackersFragment
        if (!showSuggestions) {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val horMargin = convertPXToDP(20F, context).toInt()
            val verMargin = convertPXToDP(10F, context).toInt()
            params.setMargins(horMargin, verMargin, horMargin, verMargin)
            holder.itemView.layoutParams = params
        }

        holder.binding.apply {
            trackerTitleTV.text = app.name
            if (showSuggestions) {
                chipGroup.visibility = View.VISIBLE
                trackersPB.visibility = View.GONE
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
            } else {
                val percentage = app.exodusApplications.size / trackerApps.size
                trackersStatusTV.text =
                    context.getString(
                        R.string.trackers_status,
                        percentage,
                        app.exodusApplications.size
                    )
            }
        }
    }

    private fun convertPXToDP(pixels: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            pixels,
            context.resources.displayMetrics
        )
    }
}
