package org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import dagger.hilt.android.internal.managers.FragmentComponentManager
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewTrackerItemBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.TrackersFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData

class TrackersRVAdapter(
    private val showSuggestions: Boolean,
    private val currentDestinationId: Int
) :
    ListAdapter<TrackerData, TrackersRVAdapter.ViewHolder>(TrackersDiffUtil()) {

    private val TAG = TrackersRVAdapter::class.java.simpleName

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
        val totalNumberOfAppsHavingTrackers: Int = currentList[0].totalNumberOfAppsHavingTrackers

        Log.d(
            TAG,
            "ApplicationsList in TrackerData: ${app.exodusApplications}. " +
                "Size: ${app.exodusApplications.size}."
        )

        val trackerPercentage =
            if (totalNumberOfAppsHavingTrackers != 0) {
                (app.exodusApplications.size / totalNumberOfAppsHavingTrackers.toFloat()) * 100
            } else {
                0.toFloat()
            }

        // Fix padding for TrackersFragment
        if (!showSuggestions) {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val horMargin = convertPXToDP(20, context).toInt()
            val verMargin = convertPXToDP(10, context).toInt()
            params.setMargins(horMargin, verMargin, horMargin, verMargin)
            holder.itemView.layoutParams = params
        }

        holder.binding.apply {
            trackerTitleTV.text = app.name
            if (showSuggestions) {
                chipGroup.visibility = View.VISIBLE
                trackersPB.visibility = View.GONE
                chipGroup.removeAllViews()
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
                trackersStatusTV.text =
                    context.resources.getQuantityString(
                        R.plurals.trackers_status,
                        app.exodusApplications.size,
                        trackerPercentage.toInt(),
                        app.exodusApplications.size
                    )
                trackersPB.apply {
                    val newWidth = (getDisplayWidth(context) * trackerPercentage) / 100
                    val params = layoutParams.apply {
                        width = newWidth.toInt()
                    }
                    layoutParams = params
                }
            }
            root.setOnClickListener {
                val action = if (currentDestinationId == R.id.appDetailFragment) {
                    AppDetailFragmentDirections.actionAppDetailFragmentToTrackerDetailFragment(
                        app.id,
                        trackerPercentage.toInt()
                    )
                } else {
                    TrackersFragmentDirections.actionTrackersFragmentToTrackerDetailFragment(
                        app.id,
                        trackerPercentage.toInt()
                    )
                }
                holder.itemView.findNavController().navigate(action)
            }
        }
    }

    private fun convertPXToDP(pixels: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            pixels.toFloat(),
            context.resources.displayMetrics
        )
    }

    private fun getDisplayWidth(context: Context): Int {
        val activity = FragmentComponentManager.findActivity(context) as Activity
        val windowMetrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
        return windowMetrics.bounds.width()
    }
}
