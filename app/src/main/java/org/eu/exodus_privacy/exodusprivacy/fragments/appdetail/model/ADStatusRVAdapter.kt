package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewAdStatusItemBinding
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.utils.BindingHolder
import org.eu.exodus_privacy.exodusprivacy.utils.setExodusColor

enum class ADStatusType {
    TRACKERS,
    PERMISSIONS,
}

class ADStatusRVAdapter(
    private val type: ADStatusType,
) : RecyclerView.Adapter<BindingHolder<RecyclerViewAdStatusItemBinding>>() {

    private var app: ExodusApplication = ExodusApplication()

    fun setApp(app: ExodusApplication) {
        if (this.app != app) {
            this.app = app
            notifyItemChanged(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<RecyclerViewAdStatusItemBinding> {
        return BindingHolder(
            RecyclerViewAdStatusItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: BindingHolder<RecyclerViewAdStatusItemBinding>, position: Int) {
        holder.binding.apply {
            if (type == ADStatusType.TRACKERS) {
                countChip.apply {
                    val trackerNum =
                        if (app.exodusVersionCode == 0L) "?" else app.exodusTrackers.size.toString()
                    text = trackerNum
                    setExodusColor(trackerNum)
                }
                labelTV.setText(R.string.trackers)
                if (app.exodusVersionCode == 0L) {
                    statusTV.setText(R.string.analyzed)
                    restrictionTV.visibility = View.VISIBLE
                } else if (app.exodusTrackers.isEmpty()) {
                    restrictionTV.visibility = View.GONE
                    statusTV.setText(R.string.code_signature_not_found)
                } else {
                    statusTV.setText(R.string.code_signature_found)
                }
            } else {
                val permissionsCount = app.permissions.size
                val permissionsCountString = permissionsCount.toString()
                countChip.text = permissionsCountString
                countChip.setExodusColor(permissionsCountString)
                labelTV.setText(R.string.permissions)
                if (permissionsCount == 0) {
                    statusTV.setText(R.string.code_permission_not_found)
                } else {
                    statusTV.setText(R.string.code_permission_found)
                }
            }
        }
    }
}
