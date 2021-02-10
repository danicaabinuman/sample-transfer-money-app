package com.unionbankph.corporate.auth.presentation.policy

import android.os.Bundle
import android.text.Html
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

class PrivacyPolicyFragment : BaseFragment<GeneralViewModel>(R.layout.fragment_privacy_policy) {

    override fun onViewsBound() {
        super.onViewsBound()
        textViewPolicyDesc.text = Html.fromHtml(
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
}
