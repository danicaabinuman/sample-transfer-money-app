package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentFeesAndChargesBinding


class FeesAndChargesFragment :
    BaseFragment<FragmentFeesAndChargesBinding, GeneralViewModel>() {

    companion object {

        @JvmStatic
        fun newInstance() =
            FeesAndChargesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFeesAndChargesBinding
        get() = FragmentFeesAndChargesBinding::inflate

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}