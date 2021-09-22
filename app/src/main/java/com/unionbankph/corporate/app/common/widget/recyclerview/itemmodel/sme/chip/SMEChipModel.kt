package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemSmeChipBinding
import timber.log.Timber

@EpoxyModelClass
abstract class SMEChipModel: EpoxyModelWithHolder<SMEChipModel.Holder>() {

    @EpoxyAttribute
    lateinit var model: GenericItem

    @EpoxyAttribute
    lateinit var position: String

    @EpoxyAttribute
    lateinit var callback: SMEChipCallback


    override fun getDefaultLayout(): Int {
        return R.layout.item_sme_chip
    }

    override fun bind(holder: Holder) {

        val chipModel = model

        holder.binding.apply {
            chip.text = chipModel.title?.uppercase()
            chip.isEnabled = !chipModel.isSelected!!

            chip.setOnClickListener {
                callback.onChipClicked(chipModel, position.toInt())
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemSmeChipBinding

        override fun bindView(itemView: View) {
            binding = ItemSmeChipBinding.bind(itemView)
        }
    }
}

interface SMEChipCallback {
    fun onChipClicked(genericSelection: GenericItem, position: Int) = Unit
}