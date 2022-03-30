package org.eu.exodus_privacy.exodusprivacy.fragments.apps.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewAppItemBinding
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
            appIconIV.background = app.icon.toDrawable(context.resources)
            appNameTV.text = app.name
            appVersionTV.text = context.getString(R.string.app_version, app.versionName)
            trackersChip.text = context.getString(R.string.num_trackers, app.exodusTrackers.size)
            permsChip.text = context.getString(R.string.num_perms, app.permissions.size)
        }
    }
}
