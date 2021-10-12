package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemEligibleToApplyMainBinding
import com.unionbankph.corporate.databinding.ItemFinanceWithUsMainBinding
import com.unionbankph.corporate.databinding.ItemKeyFeaturesMainBinding
import com.unionbankph.corporate.databinding.ItemLoanTermsMainBinding

@EpoxyModelClass(layout = R.layout.item_eligible_to_apply_main)
abstract class EligibleMainModel : EpoxyModelWithHolder<EligibleMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val eligible = Eligible.generateEligible(context)
            val data: MutableList<EligibleItemModel_> = mutableListOf()

            eligible.map {
                data.add(
                    EligibleItemModel_()
                        .id(it.id)
                        .dataFromContainer(it)
                        .callbacks(callbacks)
                )
            }
            eligibleClSeeList.setOnClickListener { callbacks.onSeeFullList() }
            eligibleErvData.setModels(data)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemEligibleToApplyMainBinding
        override fun bindView(itemView: View) {
            binding = ItemEligibleToApplyMainBinding.bind(itemView)
        }
    }
}