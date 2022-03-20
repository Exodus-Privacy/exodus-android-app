package org.eu.exodus_privacy.exodusprivacy.fragments.apps.model

import androidx.recyclerview.widget.DiffUtil
import org.eu.exodus_privacy.exodusprivacy.objects.Application

class AppsDiffUtil(
    private val oldList: List<Application>,
    private val newList: List<Application>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return oldList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].icon != newList[newItemPosition].icon -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].packageName != newList[newItemPosition].packageName -> false
            oldList[oldItemPosition].versionName != newList[newItemPosition].versionName -> false
            oldList[oldItemPosition].versionCode != newList[newItemPosition].versionCode -> false
            oldList[oldItemPosition].trackers != newList[newItemPosition].trackers -> false
            oldList[oldItemPosition].permissions != newList[newItemPosition].permissions -> false
            else -> true
        }
    }
}