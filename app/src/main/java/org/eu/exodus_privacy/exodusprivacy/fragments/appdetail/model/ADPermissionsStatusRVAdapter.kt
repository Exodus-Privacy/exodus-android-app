package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewPermissionStatusItemBinding
import org.eu.exodus_privacy.exodusprivacy.utils.setExodusColor

class ADPermissionsStatusRVAdapter :
    RecyclerView.Adapter<ADPermissionsStatusRVAdapter.ViewHolder>() {

    private var permissionsCount: Int = 0

    inner class ViewHolder(val binding: RecyclerViewPermissionStatusItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setPermissionCount(count: Int) {
        if (permissionsCount != count) {
            permissionsCount = count
            notifyItemChanged(0)
    }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerViewPermissionStatusItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val permsCount = permissionsCount.toString()
            permissionsChip.text = permsCount
            permissionsChip.setExodusColor(permsCount)
            if (permissionsCount == 0) {
                permissionsStatusTV.setText(R.string.code_permission_not_found)
            } else {
                permissionsStatusTV.setText(R.string.code_permission_found)
            }
        }
    }
}
