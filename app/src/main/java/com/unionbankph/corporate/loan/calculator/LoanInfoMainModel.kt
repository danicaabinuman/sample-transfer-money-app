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
    var loanAmount: Float? = 0f

    @EpoxyAttribute
    var loanTenure: Int? = 0

    @EpoxyAttribute
    var annualInterestRate: Float? = 0f

    @EpoxyAttribute
    var monthlyPayment: Float? = 0f

    @EpoxyAttribute
    var totalInterestPayable: Float? = 0f

    @EpoxyAttribute
    var totalAmountPayable: Float? = 0f

    override fun bind(holder: Holder) {
        holder.binding.apply {

            val loanInfoItem = LoanInfoItem(
                loanAmount = loanAmount,
                loanTenure = loanTenure,
                annualInterestRate = INTEREST_RATE.toFloat(),
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