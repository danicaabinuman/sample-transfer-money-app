package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemReadyToExpndFeatureCardBinding

@EpoxyModelClass(layout = R.layout.item_ready_to_expnd_feature_card)
abstract class ReadyToBusinessMainModel : EpoxyModelWithHolder<ReadyToBusinessMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    var clickListener: (View) -> Unit = { _ -> }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            cardViewFeature.setOnClickListener {
                clickListener(it)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemReadyToExpndFeatureCardBinding
        override fun bindView(itemView: View) {
            binding = ItemReadyToExpndFeatureCardBinding.bind(itemView)
        }
    }
}