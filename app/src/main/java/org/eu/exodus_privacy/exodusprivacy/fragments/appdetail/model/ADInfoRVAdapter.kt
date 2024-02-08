package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewAdInfoItemBinding
import org.eu.exodus_privacy.exodusprivacy.utils.BindingHolder

data class ADInfoItem(
    @StringRes val text: Int,
    val onClick: () -> Unit,
)

class ADInfoRVAdapter(
    private val items: List<ADInfoItem>,
) :
    RecyclerView.Adapter<BindingHolder<RecyclerViewAdInfoItemBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<RecyclerViewAdInfoItemBinding> {
        return BindingHolder(
            RecyclerViewAdInfoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BindingHolder<RecyclerViewAdInfoItemBinding>, position: Int) {
        items.getOrNull(position)?.let { infoItem ->
            holder.binding.apply {
                infoTV.setText(infoItem.text)
                learnMoreTV.setOnClickListener {
                    infoItem.onClick()
                }
            }
        }
    }
}
