package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.*

@EpoxyModelClass(layout = R.layout.item_loan_info)
abstract class LoanInfoItemModel : EpoxyModelWithHolder<LoanInfoItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromContainer: LoanInfo

    @EpoxyAttribute
    lateinit var loanAmount: String

    @EpoxyAttribute
    lateinit var loanTenure: String

    @EpoxyAttribute
    lateinit var monthlyPayment: String

    @EpoxyAttribute
    lateinit var totalInterestPayable: String

    @EpoxyAttribute
    lateinit var totalAmountPayable: String

    override fun bind(holder: Holder) {
        holder.binding.apply {
            item = dataFromContainer
            executePendingBindings()
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemLoanInfoBinding
        override fun bindView(itemView: View) {
            binding = ItemLoanInfoBinding.bind(itemView)
        }
    }
}

