package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentTermsOfServiceTabsFeesChargesBinding
import com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator.FeeCalculatorValueModel
import java.text.DecimalFormat

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FeeCharges :
    BaseFragment<FragmentTermsOfServiceTabsFeesChargesBinding, GeneralViewModel>() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terms_of_service_tabs_fees_charges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.apply {
            val feeCalculatorValueModel = FeeCalculatorValueModel(0.00)
            tablePricing.fncUnionBank.text = "PHP. 0.00"
            tablePricing.fncInstapay.text = "PHP. 0.00"
            tablePricing.fncCard.text = "PHP. 0.00"
            tablePricing.fncEWallet.text = "PHP. 0.00"
            tablePricing.fncOTC.text = "PHP. 0.00"
            val percentageFormat = DecimalFormat("##0.00")
            val feeFormat = DecimalFormat("PHP #,##0.00")
            val ubFeeValue = feeFormat.format(feeCalculatorValueModel.ubOnlineFee)
            val instapayFeeValue = feeFormat.format(feeCalculatorValueModel.instapay)
            val cardFeeValue = feeFormat.format(feeCalculatorValueModel.card)
            val cardPercentageFeeValue = percentageFormat.format(feeCalculatorValueModel.cardPercentageFee)
            val eWalletFeeValue = feeFormat.format(feeCalculatorValueModel.eWallet)
            val eWalletPercentageFeeValue = percentageFormat.format(feeCalculatorValueModel.eWalletPercentageFee)
            val otcFeeValue = feeFormat.format(feeCalculatorValueModel.instapay)

            tablePricing.fncUnionBank.text = ubFeeValue
            tablePricing.fncInstapay.text = instapayFeeValue
            tablePricing.fncCard.text = cardPercentageFeeValue + "% + " + cardFeeValue
            tablePricing.fncEWallet.text = eWalletPercentageFeeValue + "% + " + eWalletFeeValue
            tablePricing.fncOTC.text = otcFeeValue
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Page1Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeeCharges().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTermsOfServiceTabsFeesChargesBinding
        get() = FragmentTermsOfServiceTabsFeesChargesBinding::inflate

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}