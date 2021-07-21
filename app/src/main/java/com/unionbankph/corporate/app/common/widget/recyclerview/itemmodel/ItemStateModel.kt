package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemStateBinding

@EpoxyModelClass(layout = R.layout.item_state)
abstract class ItemStateModel : EpoxyModelWithHolder<ItemStateModel.Holder>() {

    @EpoxyAttribute
    var message: String? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            textViewEmptyState.text = message
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemStateBinding

        override fun bindView(itemView: View) {
            binding = ItemStateBinding.bind(itemView)
        }
    }
}
