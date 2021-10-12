package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.*

@EpoxyModelClass(layout = R.layout.item_eligible_to_apply)
abstract class EligibleItemModel : EpoxyModelWithHolder<EligibleItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromContainer: Eligible

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        holder.binding.apply {
            item = dataFromContainer
            executePendingBindings()
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemEligibleToApplyBinding
        override fun bindView(itemView: View) {
            binding = ItemEligibleToApplyBinding.bind(itemView)
        }
    }
}