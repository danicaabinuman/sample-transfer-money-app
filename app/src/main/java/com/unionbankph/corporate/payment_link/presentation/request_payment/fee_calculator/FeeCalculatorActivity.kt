package com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.databinding.ActivityFeeCalculatorBinding
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import timber.log.Timber
import java.lang.NumberFormatException
import java.text.DecimalFormat

class FeeCalculatorActivity :
    BaseActivity<ActivityFeeCalculatorBinding, FeeCalculatorViewModel>() {

    private var fromWhatTab : String? = null

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()
        initIntentData()

    }

    private fun initViews() {
        var feeAmount = 0.00
        var netAmount = 0.00

        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()


        binding.tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)
        binding.tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)
        binding.tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)

        binding.btnUB.setOnClickListener { btnUBOnClicked() }
        binding.btnInstapay.setOnClickListener { btnInstapayOnClicked() }
        binding.btnCreditDebitCard.setOnClickListener { btnCreditDebitCardOnClicked() }
        binding.btnEWallet.setOnClickListener { btnEWalletOnClicked() }
        binding.btnOverTheCounter.setOnClickListener { btnOverTheCounterOnClicked() }
        binding.btnClose.setOnClickListener { returnToRequestPayment() }
    }

    private fun initIntentData(){
        fromWhatTab = intent.getStringExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB)
        if(fromWhatTab == null){
            fromWhatTab = DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON
        }
    }

    private fun reformatStrings(){
        var feeAmount = 0.00
        var netAmount = 0.00
        val amountFormat = DecimalFormat("PHP #,##0.00")
        val feeFormat = DecimalFormat("- #,##0.00")

        val grossAmountPutExtra = intent.getStringExtra(AMOUNT_VALUE).toString()
        var grossAmountDouble = grossAmountPutExtra.toDouble()

        binding.tvGrossAmountFeeCalculator.text = amountFormat.format(grossAmountDouble)

        if (binding.btnUB.isPressed){
            feeAmount = 10.00
            netAmount = grossAmountDouble - feeAmount

            binding.btnUB.background = getDrawable(R.drawable.bg_fee_payment_method_active)
            binding.btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

        } else if (binding.btnInstapay.isPressed){
            feeAmount = 15.00
            netAmount = grossAmountDouble - feeAmount

            binding.btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnInstapay.background = getDrawable(R.drawable.bg_fee_payment_method_active)
            binding.btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

        } else if (binding.btnCreditDebitCard.isPressed){
            feeAmount = ((grossAmountDouble * .03) + 10)
            netAmount = grossAmountDouble - feeAmount

            binding.btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_payment_method_active)
            binding.btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

        } else if (binding.btnEWallet.isPressed){
            feeAmount = ((grossAmountDouble * .02) + 10)
            netAmount = grossAmountDouble - feeAmount

            binding.btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnEWallet.background = getDrawable(R.drawable.bg_fee_payment_method_active)
            binding.btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)

        } else if (binding.btnOverTheCounter.isPressed){
            feeAmount = 20.00
            netAmount = grossAmountDouble - feeAmount

            binding.btnUB.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnCreditDebitCard.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnEWallet.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnInstapay.background = getDrawable(R.drawable.bg_fee_calculator_payment_method)
            binding.btnOverTheCounter.background = getDrawable(R.drawable.bg_fee_payment_method_active)

        }


        binding.tvNetAmountFeeCalculator.text = amountFormat.format(netAmount)
        binding.tvFeeAmountFeeCalculator.text = feeFormat.format(feeAmount)

    }

    private fun btnUBOnClicked() {
        reformatStrings()
    }

    private fun btnInstapayOnClicked() {
        reformatStrings()
    }

    private fun btnCreditDebitCardOnClicked() {
        reformatStrings()
    }

    private fun btnEWalletOnClicked() {
        reformatStrings()
    }

    private fun btnOverTheCounterOnClicked() {
        reformatStrings()
    }

    private fun returnToRequestPayment(){

        navigator.navigate(
            this,
            RequestForPaymentActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_DOWN
        )

        finish()
    }

    override fun onBackPressed() {
        returnToRequestPayment()
    }

    companion object{

        const val AMOUNT_VALUE = "VALUE"
        const val FROM_WHAT_TAB = "from_what_tab"
    }

    override val viewModelClassType: Class<FeeCalculatorViewModel>
        get() = FeeCalculatorViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityFeeCalculatorBinding
        get() = ActivityFeeCalculatorBinding::inflate


}