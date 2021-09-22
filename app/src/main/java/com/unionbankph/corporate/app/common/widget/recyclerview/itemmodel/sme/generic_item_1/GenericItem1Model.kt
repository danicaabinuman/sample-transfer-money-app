package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.generic_item_1

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.GenericItem
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.ItemGenericItem1Binding

@EpoxyModelClass
abstract class GenericItem1Model : EpoxyModelWithHolder<GenericItem1Model.Holder>() {

    @EpoxyAttribute
    lateinit var model: GenericItem

    @EpoxyAttribute
    lateinit var context: Context

    override fun getDefaultLayout(): Int {
        return R.layout.item_generic_item_1
    }

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val imageSourceId = context.resources.getIdentifier(
                model.src, "drawable", Constant.PACKAGE_NAME
            )

            imageViewIcon.setImageResource(imageSourceId)

            textViewLabel.text = model.title
            textViewCaption.text = model.subtitle
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var binding: ItemGenericItem1Binding

        override fun bindView(itemView: View) {
            binding = ItemGenericItem1Binding.bind(itemView)
        }
    }
}