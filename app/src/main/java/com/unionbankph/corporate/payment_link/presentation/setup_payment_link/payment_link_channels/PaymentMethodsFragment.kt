package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment


class PaymentMethodsFragment :
    BaseFragment<PaymentMethodsViewModel>(R.layout.fragment_payment_methods) {

    companion object {

        @JvmStatic
        fun newInstance() =
            PaymentMethodsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}