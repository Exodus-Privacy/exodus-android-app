package org.eu.exodus_privacy.exodusprivacy.fragments.apps.model

import androidx.recyclerview.widget.DiffUtil
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication

class AppsDiffUtil : DiffUtil.ItemCallback<ExodusApplication>() {

    override fun areItemsTheSame(oldItem: ExodusApplication, newItem: ExodusApplication): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(
        oldItem: ExodusApplication,
        newItem: ExodusApplication,
    ): Boolean {
        return when {
            oldItem.icon != newItem.icon -> false
            oldItem.name != newItem.name -> false
            oldItem.packageName != newItem.packageName -> false
            oldItem.versionName != newItem.versionName -> false
            oldItem.versionCode != newItem.versionCode -> false
            oldItem.permissions != newItem.permissions -> false
            else -> true
        }
    }
}
