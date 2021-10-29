package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemMonthlyPaymentMainBinding
import timber.log.Timber
import kotlin.math.roundToInt

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
    var amount: Float? = null

    @EpoxyAttribute
    var months: Int? = null

    @EpoxyAttribute
    var amountChangeListener: (Int) -> Unit = { _ -> }

    @EpoxyAttribute
    var applyNowItemClickListener: (View) -> Unit = { _ -> }

    @EpoxyAttribute
    lateinit var callback: LoansCalculatorCallback

    //TODO REMOVE EPOXY RECYCLERVIEW
    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            /*item = dataTest
            executePendingBindings()*/

            //TODO CLEANUP
            monthlyPaymentBApplyNow.setOnClickListener {
                //applyNowItemClickListener(it)
                callback.onApplyNow()
            }

            if (months != 0 && amount != 0f ) {

                loanAmount = amount
                loanMonths = months

                seekBar.progress = amount?.toInt()!!

                monthlyPaymentMbtgHowLong.apply {
                    when (months) {
                        3 -> check(R.id.monthly_payment_b_three)
                        6 -> check(R.id.monthly_payment_b_six)
                        12 -> check(R.id.monthly_payment_b_twelve)
                        24 -> check(R.id.monthly_payment_b_twenty_four)
                        36 -> check(R.id.monthly_payment_b_thirty_six)
                    }
                }

                val principal = months?.let { amount?.div(it) }
                val interest = months?.let { (amount?.times(0.36))?.div(it)?.toFloat() }
                val monthlyPayment = interest?.let { principal?.plus(it) }

                principal?.let { callback.onPrincipal(it) }
                interest?.let { callback.onInterest(it) }

                monthlyPaymentTvAmount.text = context.getString(R.string.format_php, monthlyPayment)
                monthlyPayment?.let { callback.onMonthlyPayment(it) }
                amount?.plus(amount!!.times(0.36))?.let { callback.onTotalAmountPayable(it.toFloat()) }

                amount?.let { autoFormatEditText.setText(it.toString()) }
            }

            if (amount == 0f){
                seekBar.progress = 50000
            }

            if (months == 0) {
                monthlyPaymentMbtgHowLong.clearChecked()
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                            val test = ((progress / 10000).toDouble().roundToInt()).times(10000)
                            callback.onAmountChange(test.toFloat())
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            handler = object : LoansCalculatorHandler {
                override fun onMonths(months: Int) {
                    callback.onMonthsChange(months)
                }
            }
            executePendingBindings()
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemMonthlyPaymentMainBinding
        override fun bindView(itemView: View) {
            binding = ItemMonthlyPaymentMainBinding.bind(itemView)
        }
    }

}