package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewPermissionItemBinding
import org.eu.exodus_privacy.exodusprivacy.objects.Permission

class ADPermissionsRVAdapter :
    ListAdapter<Permission, ADPermissionsRVAdapter.ViewHolder>(ADPermissionsDiffUtil()) {

    inner class ViewHolder(val binding: RecyclerViewPermissionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerViewPermissionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)

        holder.binding.apply {
            permissionTitleTV.text = app.shortName
            permissionSubTitleTV.text = app.label.replaceFirstChar { it.uppercase() }
        }
    }
}
