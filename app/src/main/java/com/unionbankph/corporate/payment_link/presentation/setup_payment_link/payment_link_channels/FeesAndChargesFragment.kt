package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment


class FeesAndChargesFragment :
    BaseFragment<FeesAndChargesViewModel>(R.layout.fragment_fees_and_charges) {

    companion object {

        @JvmStatic
        fun newInstance() =
            FeesAndChargesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}