package com.unionbankph.corporate.auth.presentation.policy

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment :
    BaseFragment<FragmentPrivacyPolicyBinding, GeneralViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        binding.textViewPolicyDesc.text = Html.fromHtml(
            viewUtil.getFileText(
                (activity as PrivacyPolicyActivity),
                "privacy"
            )
        )
    }

    companion object {
        fun newInstance(): PrivacyPolicyFragment {
            val fragment =
                PrivacyPolicyFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPrivacyPolicyBinding
        get() = FragmentPrivacyPolicyBinding::inflate
}
