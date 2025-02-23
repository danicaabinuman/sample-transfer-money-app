package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentPaymentMethodsBinding


class PaymentMethodsFragment :
    BaseFragment<FragmentPaymentMethodsBinding, GeneralViewModel>() {

    companion object {

        @JvmStatic
        fun newInstance() =
            PaymentMethodsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPaymentMethodsBinding
        get() = FragmentPaymentMethodsBinding::inflate

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}