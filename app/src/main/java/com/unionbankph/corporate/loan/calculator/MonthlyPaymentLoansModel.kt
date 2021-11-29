package com.unionbankph.corporate.loan.calculator

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.airbnb.epoxy.*
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.databinding.ItemMonthlyPaymentMainBinding
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.concurrent.TimeUnit
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
    @SuppressLint("CheckResult")
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
            } else {
                monthlyPaymentTvAmount.text = context.getString(R.string.format_php, 0f)
            }

            if (amount != 0f) {
                amount?.let { autoFormatEditText.setText(amount!!.toInt().toString()) }
            } else {
                amount?.let { autoFormatEditText.setText("0") }
                seekBar.progress = 50000
            }

            if (months == 0) {
                monthlyPaymentMbtgHowLong.clearChecked()
            }

            //TODO
            autoFormatEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val cleanString = s.toString().replace(",","")
                    var amountDouble = 0.00
                    try {
                        amountDouble = cleanString.toDouble()
                        if(amountDouble in 50000.00..1000000.00) {
                            //autoFormatEditText.setText(amountDouble.toString())
                            callback.onAmountChange(amountDouble.toFloat())
                        } else if (amountDouble >= 1000000.00){
                            callback.onAmountChange(1000000.00f)
                        } else if (amountDouble == 0.00){
                            callback.onAmountChange(0.00f)
                        }
                    }catch (e: NumberFormatException){
                        Timber.e(e)
                        e.printStackTrace()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            })

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                            val seekBarAmount = ((progress / 10000).toDouble().roundToInt()).times(10000)
                            callback.onAmountChange(seekBarAmount.toFloat())
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