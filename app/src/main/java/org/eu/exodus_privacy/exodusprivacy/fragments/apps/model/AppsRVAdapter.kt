package org.eu.exodus_privacy.exodusprivacy.fragments.apps.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewAppItemBinding
import org.eu.exodus_privacy.exodusprivacy.objects.Application

class AppsRVAdapter : RecyclerView.Adapter<AppsRVAdapter.ViewHolder>() {

    private var oldList = emptyList<Application>()

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
        val app = oldList[position]

        holder.binding.apply {
            appIconIV.background = app.icon
            appNameTV.text = app.name
            appVersionTV.text = context.getString(R.string.app_version, app.versionName)
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<Application>) {
        val diffUtil = AppsDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}