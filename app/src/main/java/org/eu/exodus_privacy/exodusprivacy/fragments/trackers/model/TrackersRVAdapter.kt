package org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewTrackerItemBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.TrackersFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.utils.BindingHolder
import org.eu.exodus_privacy.exodusprivacy.utils.safeNavigate

class TrackersRVAdapter(
    private val showSuggestions: Boolean,
    private val currentDestinationId: Int,
) :
    ListAdapter<TrackerData, BindingHolder<RecyclerViewTrackerItemBinding>>(TrackersDiffUtil()) {

    private val TAG = TrackersRVAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<RecyclerViewTrackerItemBinding> {
        return BindingHolder(
            RecyclerViewTrackerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: BindingHolder<RecyclerViewTrackerItemBinding>, position: Int) {
        val context = holder.itemView.context

        val app = getItem(position)
        val totalNumberOfAppsHavingTrackers: Int = currentList[0].totalNumberOfAppsHavingTrackers

        Log.d(
            TAG,
            "ApplicationsList in TrackerData: ${app.exodusApplications}. " + "Size: ${app.exodusApplications.size}.",
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
                RelativeLayout.LayoutParams.WRAP_CONTENT,
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
                        R.style.Theme_Exodus_Chip,
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
                        app.exodusApplications.size,
                    )
                trackersPB.apply {
                    post {
                        val newWidth = (holder.itemView.width * trackerPercentage) / 100
                        val params = layoutParams.apply {
                            width = newWidth.toInt()
                        }
                        layoutParams = params
                    }
                }
            }
            root.setOnClickListener {
                if (currentDestinationId != 0) {
                    val action =
                        if (currentDestinationId == R.id.appDetailFragment) {
                            AppDetailFragmentDirections.actionAppDetailFragmentToTrackerDetailFragment(
                                app.id,
                                trackerPercentage.toInt(),
                            )
                        } else {
                            TrackersFragmentDirections.actionTrackersFragmentToTrackerDetailFragment(
                                app.id,
                                trackerPercentage.toInt(),
                            )
                        }
                    holder.itemView.findNavController().safeNavigate(action)
                }
            }
        }
    }

    private fun convertPXToDP(pixels: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            pixels.toFloat(),
            context.resources.displayMetrics,
        )
    }
}
