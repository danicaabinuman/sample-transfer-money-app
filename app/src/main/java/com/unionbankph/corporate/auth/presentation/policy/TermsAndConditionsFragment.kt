package com.unionbankph.corporate.auth.presentation.policy

import android.os.Bundle
import android.text.Html
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import kotlinx.android.synthetic.main.fragment_terms_conditions.*

class TermsAndConditionsFragment :
    BaseFragment<GeneralViewModel>(R.layout.fragment_terms_conditions) {

    override fun onViewsBound() {
        super.onViewsBound()
        textViewTermsConditions.text = Html.fromHtml(
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
}
