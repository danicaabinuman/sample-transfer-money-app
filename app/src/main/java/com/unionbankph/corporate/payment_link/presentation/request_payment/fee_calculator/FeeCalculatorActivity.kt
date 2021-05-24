package com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator

import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_fee_calculator.*
import timber.log.Timber
import java.lang.NumberFormatException
import java.text.DecimalFormat

class FeeCalculatorActivity :
    BaseActivity<FeeCalculatorViewModel>(R.layout.activity_fee_calculator) {

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()
    }

    private fun initViews() {
        var feeAmount = 0.00
        var grossAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        grossAmount = intent.getStringExtra("value")!!.toDouble()

        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmount.toString())
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())

        btnUB.setOnClickListener { btnUBOnClicked() }
        btnInstapay.setOnClickListener { btnInstapayOnClicked() }
        btnCreditDebitCard.setOnClickListener { btnCreditDebitCardOnClicked() }
        btnEWallet.setOnClickListener { btnEWalletOnClicked() }
        btnOverTheCounter.setOnClickListener { btnOverTheCounterOnClicked() }
    }

    private fun btnUBOnClicked() {

        var feeAmount = 10.00
        var grossAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())

        try {

            grossAmount = intent.getStringExtra("value")!!.toDouble()
            tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmount.toString())

        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        netAmount = grossAmount - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())
        btnUB.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnInstapayOnClicked() {

        var feeAmount = 15.00
        var grossAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        try {

            grossAmount = intent.getStringExtra("value")!!.toDouble()
            tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmount.toString())

        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        netAmount = grossAmount - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())
        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnCreditDebitCardOnClicked() {

        var feeAmount = 10.00
        var grossAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        try {

            grossAmount = intent.getStringExtra("value")!!.toDouble()
            tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmount.toString())

        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        netAmount = grossAmount - ((grossAmount * .03) + feeAmount)
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())
        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnEWalletOnClicked() {

        var feeAmount = 10.00
        var grossAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        try {

            grossAmount = intent.getStringExtra("value")!!.toDouble()
            tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmount.toString())

        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        netAmount = grossAmount - ((grossAmount * .02) + feeAmount)
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())
        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnOverTheCounterOnClicked() {

        var feeAmount = 20.00
        var grossAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        try {

            grossAmount = intent.getStringExtra("value")!!.toDouble()
            tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmount.toString())

        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount.toString())
        netAmount = grossAmount - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount.toString())
        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_payment_method_active)

    }


}