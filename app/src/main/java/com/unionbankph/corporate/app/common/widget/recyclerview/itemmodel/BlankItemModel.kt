package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemBlankBinding

@EpoxyModelClass
abstract class BlankItemModel : EpoxyModelWithHolder<BlankItemModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.item_blank
    }

    override fun bind(holder: Holder) {

    }

    class Holder: EpoxyHolder() {

        lateinit var binding : ItemBlankBinding

        override fun bindView(itemView: View) {
            binding = ItemBlankBinding.bind(itemView)
        }
    }
}