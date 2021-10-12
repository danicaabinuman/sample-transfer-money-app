package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemCommonQuestionsMainBinding
import com.unionbankph.corporate.databinding.ItemFinanceWithUsMainBinding
import com.unionbankph.corporate.databinding.ItemKeyFeaturesMainBinding
import com.unionbankph.corporate.databinding.ItemLoanInfoMainBinding

@EpoxyModelClass(layout = R.layout.item_loan_info_main)
abstract class LoanInfoMainModel : EpoxyModelWithHolder<LoanInfoMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromViewModel: List<LoanInfo>

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val loanInfo = LoanInfo.generateLoanInfo(context)
            val data: MutableList<LoanInfoItemModel_> = mutableListOf()

            loanInfo.map {
                data.add(
                    LoanInfoItemModel_()
                        .id(it.id)
                        .dataFromContainer(it)
                )
            }
            loanInfoErvData.setModels(data)
/*            dataFromViewModel.map {
                data.add(
                    LoanInfoMainModel_()
                    .id(it.id)
                    .dataFromContainer(it)
                )
            }
            monthlyPaymentErvData.setModels(data)*/
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemLoanInfoMainBinding
        override fun bindView(itemView: View) {
            binding = ItemLoanInfoMainBinding.bind(itemView)
        }
    }
}