package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewPermissionInfoItemBinding

class ADPermissionsInfoRVAdapter(
    private val googleInfoClicked: () -> Unit,
    private val permissionInfoClicked: () -> Unit,
) :
    RecyclerView.Adapter<ADPermissionsInfoRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RecyclerViewPermissionInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerViewPermissionInfoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        binding.permissionsLearnGoogleTV.setOnClickListener {
            googleInfoClicked()
        }
        binding.permissionsLearnExodusTV.setOnClickListener {
            permissionInfoClicked()
        }
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // nothing to do
    }
}
