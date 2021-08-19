package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.databinding.FragmentTermsOfServiceTabsFeesChargesBinding
import com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator.FeeCalculatorValueModel
import java.text.DecimalFormat

class FeeCharges :
    BaseFragment<FragmentTermsOfServiceTabsFeesChargesBinding, TermsOfServiceViewModel>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.tablePricing.apply {
            val feeCalculatorValueModel = FeeCalculatorValueModel(0.00)
            fncUnionBank.text = "PHP. 0.00"
            fncInstapay.text = "PHP. 0.00"
            fncCard.text = "PHP. 0.00"
            fncEWallet.text = "PHP. 0.00"
            fncOTC.text = "PHP. 0.00"
            val percentageFormat = DecimalFormat("##0.00")
            val feeFormat = DecimalFormat("PHP #,##0.00")
            val ubFeeValue = feeFormat.format(feeCalculatorValueModel.ubOnlineFee)
            val instapayFeeValue = feeFormat.format(feeCalculatorValueModel.instapay)
            val cardFeeValue = feeFormat.format(feeCalculatorValueModel.card)
            val cardPercentageFeeValue = percentageFormat.format(feeCalculatorValueModel.cardPercentageFee)
            val eWalletFeeValue = feeFormat.format(feeCalculatorValueModel.eWallet)
            val eWalletPercentageFeeValue = percentageFormat.format(feeCalculatorValueModel.eWalletPercentageFee)
            val otcFeeValue = feeFormat.format(feeCalculatorValueModel.otc)

            fncUnionBank.text = ubFeeValue
            fncInstapay.text = instapayFeeValue
            fncCard.text = cardPercentageFeeValue + "% + " + cardFeeValue
            fncEWallet.text = eWalletPercentageFeeValue + "% + " + eWalletFeeValue
            fncOTC.text = otcFeeValue
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTermsOfServiceTabsFeesChargesBinding
        get() = FragmentTermsOfServiceTabsFeesChargesBinding::inflate

    override val viewModelClassType: Class<TermsOfServiceViewModel>
        get() = TermsOfServiceViewModel::class.java
}