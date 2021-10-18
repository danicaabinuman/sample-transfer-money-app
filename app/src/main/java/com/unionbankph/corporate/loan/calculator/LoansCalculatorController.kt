package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.util.Log
import android.view.View
import com.airbnb.epoxy.*
import com.google.android.material.slider.Slider
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.dashboard.fragment.DashboardAdapterCallback
import com.unionbankph.corporate.databinding.ItemMonthlyPaymentMainBinding
import com.unionbankph.corporate.itemMonthlyPaymentMain
import com.unionbankph.corporate.loan.applyloan.LoansAdapterCallback
import com.unionbankph.corporate.mcd.data.model.SectionedCheckDepositLogs
import com.unionbankph.corporate.monthlyPaymentMain


class LoansCalculatorController
constructor(
    private val context: Context
) : TypedEpoxyController<LoanCalculatorState>() {


    @AutoModel
    lateinit var monthlyPaymentBreakdownMainModel: MonthlyPaymentBreakdownMainModel_

    @AutoModel
    lateinit var loanInfoMainModel: LoanInfoMainModel_

    private lateinit var callback: LoansCalculatorCallback

    var clickListener: (Int) -> Unit = { _ -> }

    var applyNowClickListener: (View) -> Unit = { _ -> }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(loanCalculatorState: LoanCalculatorState?) {

        loanCalculatorState?.let {

            MonthlyPaymentLoansModel_()
                .context(context)
                .id("monthly-payment-loans")
                .amount(it.amount ?: 0)
                .months(it.month ?: 0)
                .callback(callback)
                .addTo(this)

            monthlyPaymentBreakdownMainModel
                .context(context)
                .principal(it.principal)
                .interest(it.interest)
                .monthlyPayment(it.monthlyPayment)
                .addIf(it.principal != 0 && it.interest != 0, this)

            loanInfoMainModel
                .context(context)
                .loanAmount(it.amount)
                .loanTenure(it.month)
                .annualInterestRate(it.interest)
                .monthlyPayment(it.monthlyPayment)
                .totalInterestPayable(it.monthlyPayment)
                .totalAmountPayable(it.totalAmountPayable)
                .addIf(it.principal != 0 && it.interest != 0, this)
        }
    }

    fun setLoansCallbacks(callback: LoansCalculatorCallback) {
        this.callback = callback
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }


}




