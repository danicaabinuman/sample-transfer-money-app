package com.unionbankph.corporate.auth.presentation.policy

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentTermsConditionsBinding

class TermsAndConditionsFragment :
    BaseFragment<FragmentTermsConditionsBinding, GeneralViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        binding.textViewTermsConditions.text = Html.fromHtml(
            viewUtil.getFileText(
                (activity as PrivacyPolicyActivity),
                "terms"
            )
        )
    }

    companion object {
        fun newInstance(): TermsAndConditionsFragment {
            val fragment =
                TermsAndConditionsFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTermsConditionsBinding
        get() = FragmentTermsConditionsBinding::inflate
}
