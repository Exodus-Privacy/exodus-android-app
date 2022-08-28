package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import androidx.recyclerview.widget.DiffUtil
import org.eu.exodus_privacy.exodusprivacy.objects.Permission

class ADPermissionsDiffUtil : DiffUtil.ItemCallback<Permission>() {
    override fun areItemsTheSame(oldItem: Permission, newItem: Permission): Boolean {
        return oldItem.permission == newItem.permission
    }

    override fun areContentsTheSame(oldItem: Permission, newItem: Permission): Boolean {
        return when {
            oldItem.permission != newItem.permission -> false
            oldItem.label != newItem.label -> false
            oldItem.description != newItem.description -> false
            else -> true
        }
    }
}
