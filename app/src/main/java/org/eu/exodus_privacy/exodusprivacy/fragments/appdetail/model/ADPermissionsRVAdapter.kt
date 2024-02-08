package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewPermissionItemBinding
import org.eu.exodus_privacy.exodusprivacy.objects.Permission
import org.eu.exodus_privacy.exodusprivacy.utils.BindingHolder

class ADPermissionsRVAdapter :
    ListAdapter<Permission, BindingHolder<RecyclerViewPermissionItemBinding>>(ADPermissionsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<RecyclerViewPermissionItemBinding> {
        return BindingHolder(
            RecyclerViewPermissionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: BindingHolder<RecyclerViewPermissionItemBinding>, position: Int) {
        val app = getItem(position)

        holder.binding.apply {
            permissionTitleTV.text = app.shortName
            permissionSubTitleTV.text = app.label.replaceFirstChar { it.uppercase() }
        }
    }
}
