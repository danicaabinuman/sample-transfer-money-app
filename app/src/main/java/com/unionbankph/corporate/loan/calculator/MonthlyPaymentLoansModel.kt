package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemMonthlyPaymentMainBinding

@EpoxyModelClass
abstract class MonthlyPaymentLoansModel(
) : EpoxyModelWithHolder<MonthlyPaymentLoansModel.Holder>() {

   override fun getDefaultLayout(): Int {
       return R.layout.item_monthly_payment_main
   }

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataTest: LoanCalculatorState

    @EpoxyAttribute
    var amount: Int? = null

    @EpoxyAttribute
    var months: Int? = null

    @EpoxyAttribute
    var amountChangeListener: (Int) -> Unit = { _ -> }

    @EpoxyAttribute
    var applyNowItemClickListener: (View) -> Unit = { _ -> }

    @EpoxyAttribute
    lateinit var callback: LoansCalculatorCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            /*item = dataTest
            executePendingBindings()*/

            monthlyPaymentBApplyNow.setOnClickListener {
                //applyNowItemClickListener(it)
                callback.onApplyNow()
            }

            if (months != 0 && amount != 0 ) {
                val principal = months?.let { amount?.div(it) }
                val interest = months?.let { (amount?.times(0.36))?.div(it) }
                val monthlyPayment = interest?.let { principal?.plus(it) }

                principal?.let { callback.onPrincipal(it) }
                interest?.let { callback.onInterest(it.toInt()) }

                monthlyPaymentTvAmount.text = context.getString(R.string.format_php, monthlyPayment?.toInt())
                monthlyPayment?.toInt()?.let { callback.onMonthlyPayment(it) }
                amount?.plus(amount!!.times(0.36))?.let { callback.onTotalAmountPayable(it.toInt()) }
            }

            amount?.let { monthlyPaymentAfetAmount.setText(it.toString()) }

            monthlyPaymentSbAmount.addOnChangeListener { slider, value, fromUser ->
                callback.onAmountChange(value.toInt())
            }

            handler = object : LoansCalculatorHandler {
                override fun onMonths(months: Int) {
                    callback.onMonthsChange(months)
                }
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemMonthlyPaymentMainBinding
        override fun bindView(itemView: View) {
            binding = ItemMonthlyPaymentMainBinding.bind(itemView)
        }
    }


}