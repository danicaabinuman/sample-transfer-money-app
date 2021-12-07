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

    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTermsOfServiceTabsFeesChargesBinding
        get() = FragmentTermsOfServiceTabsFeesChargesBinding::inflate

    override val viewModelClassType: Class<TermsOfServiceViewModel>
        get() = TermsOfServiceViewModel::class.java
}