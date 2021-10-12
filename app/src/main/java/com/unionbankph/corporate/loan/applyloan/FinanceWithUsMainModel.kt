package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemFinanceWithUsMainBinding
import com.unionbankph.corporate.databinding.ItemKeyFeaturesMainBinding

@EpoxyModelClass(layout = R.layout.item_finance_with_us_main)
abstract class FinanceWithUsMainModel : EpoxyModelWithHolder<FinanceWithUsMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val generateFinanceWithUs = FinanceWithUs.generateFinanceWithUs(context)
            val financeWithUsData: MutableList<FinanceWithUsItemModel_> = mutableListOf()

            generateFinanceWithUs.map {
                financeWithUsData.add(
                    FinanceWithUsItemModel_()
                        .id(it.id)
                        .dataFromContainer(it)
                        .callbacks(callbacks)
                )
            }
            financeErvData.setModels(financeWithUsData)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemFinanceWithUsMainBinding
        override fun bindView(itemView: View) {
            binding = ItemFinanceWithUsMainBinding.bind(itemView)
        }
    }
}