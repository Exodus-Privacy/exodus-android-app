package org.eu.exodus_privacy.exodusprivacy.fragments.apps.model

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewAppItemBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.apps.AppsFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication

class AppsRVAdapter : ListAdapter<ExodusApplication, AppsRVAdapter.ViewHolder>(AppsDiffUtil()) {

    inner class ViewHolder(val binding: RecyclerViewAppItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerViewAppItemBinding.inflate(
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
            root.setOnClickListener {
                val action =
                    AppsFragmentDirections.actionAppsFragmentToAppDetailFragment(app.packageName)
                it.findNavController().navigate(action)
            }
            appIconIV.background = app.icon.toDrawable(context.resources)
            appNameTV.text = app.name
            appVersionTV.text = context.getString(R.string.app_version, app.versionName)
            trackersChip.apply {
                val trackerNum = app.exodusTrackers.size
                text = trackerNum.toString()
                setExodusColor(trackerNum)
            }
            permsChip.apply {
                val permsNum = app.permissions.size
                text = permsNum.toString()
                setExodusColor(permsNum)
            }
            versionChip.chipIcon = when (app.exodusVersionCode) {
                app.versionCode -> ContextCompat.getDrawable(context, R.drawable.ic_match)
                0L -> ContextCompat.getDrawable(context, R.drawable.ic_unavailable)
                else -> ContextCompat.getDrawable(context, R.drawable.ic_mismatch)
            }
        }
    }

    private fun Chip.setExodusColor(size: Int) {
        val colorRed = ContextCompat.getColor(context, R.color.colorRedLight)
        val colorYellow = ContextCompat.getColor(context, R.color.colorYellow)
        val colorGreen = ContextCompat.getColor(context, R.color.colorGreen)

        val colorStateList = when (size) {
            0 -> ColorStateList.valueOf(colorGreen)
            in 1..4 -> ColorStateList.valueOf(colorYellow)
            else -> ColorStateList.valueOf(colorRed)
        }
        this.chipBackgroundColor = colorStateList
    }
}
