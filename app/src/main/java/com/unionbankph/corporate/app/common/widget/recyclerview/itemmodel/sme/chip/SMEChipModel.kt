package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemSmeChipBinding

@EpoxyModelClass
abstract class SMEChipModel: EpoxyModelWithHolder<SMEChipModel.Holder>() {

    @EpoxyAttribute
    lateinit var model: ChipItemModel

    override fun getDefaultLayout(): Int {
        return R.layout.item_sme_chip
    }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            chipItem.text = model.label
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemSmeChipBinding

        override fun bindView(itemView: View) {
            binding = ItemSmeChipBinding.bind(itemView)
        }
    }
}