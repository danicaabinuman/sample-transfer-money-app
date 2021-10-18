package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.icu.text.NumberFormat
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemLoanInfoMainBinding
import com.unionbankph.corporate.itemLoanInfoBinding
import java.util.*

@EpoxyModelClass(layout = R.layout.item_loan_info_main)
abstract class LoanInfoMainModel : EpoxyModelWithHolder<LoanInfoMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromViewModel: List<LoanInfo>

    @EpoxyAttribute
    var loanAmount: Int? = 0

    @EpoxyAttribute
    var loanTenure: Int? = 0

    @EpoxyAttribute
    var annualInterestRate: Int? = 0

    @EpoxyAttribute
    var monthlyPayment: Int? = 0

    @EpoxyAttribute
    var totalInterestPayable: Int? = 0

    @EpoxyAttribute
    var totalAmountPayable: Int? = 0

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val loanInfoItem = LoanInfoItem(
                loanAmount = loanAmount,
                loanTenure = loanTenure,
                annualInterestRate = INTEREST_RATE,
                monthlyPayment = monthlyPayment,
                totalInterestPayable = annualInterestRate,
                totalAmountPayable = totalAmountPayable
            )

            loanInfoErvData.withModels {
                itemLoanInfoBinding {
                    id(hashCode())
                    item(loanInfoItem)
                }
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemLoanInfoMainBinding
        override fun bindView(itemView: View) {
            binding = ItemLoanInfoMainBinding.bind(itemView)
        }
    }

    companion object {
        const val INTEREST_RATE = 36
    }
}