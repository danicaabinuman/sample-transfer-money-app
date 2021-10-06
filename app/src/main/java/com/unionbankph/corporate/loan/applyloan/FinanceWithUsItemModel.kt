package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemFinanceWithUsBinding
import com.unionbankph.corporate.databinding.ItemKeyFeaturesItemBinding

@EpoxyModelClass(layout = R.layout.item_finance_with_us)
abstract class FinanceWithUsItemModel : EpoxyModelWithHolder<FinanceWithUsItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromContainer: FinanceWithUs

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            //itemKeyFeaturesClParent.setOnClickListener { callbacks.onKeyFeatures(dataFromContainer.title) }
            holder.binding.item = dataFromContainer
            holder.binding.executePendingBindings()
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemFinanceWithUsBinding
        override fun bindView(itemView: View) {
            binding = ItemFinanceWithUsBinding.bind(itemView)
        }
    }
}

