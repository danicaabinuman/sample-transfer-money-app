package com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator

import android.os.Bundle
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
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()


        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)

        btnUB.setOnClickListener { btnUBOnClicked() }
        btnInstapay.setOnClickListener { btnInstapayOnClicked() }
        btnCreditDebitCard.setOnClickListener { btnCreditDebitCardOnClicked() }
        btnEWallet.setOnClickListener { btnEWalletOnClicked() }
        btnOverTheCounter.setOnClickListener { btnOverTheCounterOnClicked() }
        btnClose.setOnClickListener { finish() }
    }

    private fun btnUBOnClicked() {

        var feeAmount = 10
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()


        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        netAmount = grossAmountDouble - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)

        btnUB.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnInstapayOnClicked() {

        var feeAmount = 15
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()


        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        netAmount = grossAmountDouble - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)
        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnCreditDebitCardOnClicked() {

        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()
        var feeAmount = ((grossAmountDouble * .03) + 10)


        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        netAmount = grossAmountDouble - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)

        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnEWalletOnClicked() {

        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()
        var feeAmount = ((grossAmountDouble * .02) + 10)


        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        netAmount = grossAmountDouble - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)

        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_payment_method_active)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

    }

    private fun btnOverTheCounterOnClicked() {

        var feeAmount = 10
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()


        tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        netAmount = grossAmountDouble - feeAmount
        tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)

        btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
        btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_payment_method_active)

    }

    companion object{

        const val AMOUNT_VALUE = "VALUE"
    }


}