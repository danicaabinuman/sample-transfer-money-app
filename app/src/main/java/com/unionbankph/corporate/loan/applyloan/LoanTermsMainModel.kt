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
import com.unionbankph.corporate.databinding.ItemLoanTermsMainBinding

@EpoxyModelClass(layout = R.layout.item_loan_terms_main)
abstract class LoanTermsMainModel : EpoxyModelWithHolder<LoanTermsMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val loanTerms = LoanTerms.generateLoanTerms(context)
            val data: MutableList<LoanTermsItemModel_> = mutableListOf()

            loanTerms.map {
                data.add(
                    LoanTermsItemModel_()
                        .id(it.id)
                        .dataFromContainer(it)
                        .callbacks(callbacks)
                )
            }
            holder.binding.loanTermsErvData.setModels(data)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemLoanTermsMainBinding
        override fun bindView(itemView: View) {
            binding = ItemLoanTermsMainBinding.bind(itemView)
        }
    }
}