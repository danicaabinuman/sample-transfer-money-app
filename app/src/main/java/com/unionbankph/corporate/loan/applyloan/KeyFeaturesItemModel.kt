package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemKeyFeaturesItemBinding

@EpoxyModelClass(layout = R.layout.item_key_features_item)
abstract class KeyFeaturesItemModel : EpoxyModelWithHolder<KeyFeaturesItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromContainer: KeyFeatures

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            itemKeyFeaturesClParent.setOnClickListener { callbacks.onKeyFeatures(dataFromContainer.title) }
            item = dataFromContainer
            executePendingBindings()
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemKeyFeaturesItemBinding
        override fun bindView(itemView: View) {
            binding = ItemKeyFeaturesItemBinding.bind(itemView)
        }
    }
}

