package com.unionbankph.corporate.loan.calculator

import android.content.Context
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

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            /*item = dataTest
            executePendingBindings()*/

            monthlyPaymentBApplyNow.setOnClickListener {
                //applyNowItemClickListener(it)
                callback.onApplyNow()
            }

            if (months != 0 && amount != 0f ) {
                val principal = months?.let { amount?.div(it) }
                val interest = months?.let { (amount?.times(0.36))?.div(it)?.toFloat() }
                val monthlyPayment = interest?.let { principal?.plus(it) }

                principal?.let { callback.onPrincipal(it) }
                interest?.let { callback.onInterest(it) }

                monthlyPaymentTvAmount.text = context.getString(R.string.format_php, monthlyPayment)
                monthlyPayment?.let { callback.onMonthlyPayment(it) }
                amount?.plus(amount!!.times(0.36))?.let { callback.onTotalAmountPayable(it.toFloat()) }
            }

            amount?.let { autoFormatEditText.setText(it.toString()) }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    //callback.onAmountChange(progress.times(10000).toFloat())
                    if (fromUser) {
                            val test = ((progress / 10000).toDouble().roundToInt()).times(10000)
                            callback.onAmountChange(test.toFloat())
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            //Material Design
            /*seekBar.setOnSeekBarChangeListener { slider, value, fromUser ->
                callback.onAmountChange(value)
            }*/

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