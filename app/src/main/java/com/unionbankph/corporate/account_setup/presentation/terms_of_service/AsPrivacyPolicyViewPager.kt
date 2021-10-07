package com.unionbankph.corporate.account_setup.presentation.terms_of_service

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.databinding.ViewPagerAsPrivacyPolicyBinding
import timber.log.Timber

class AsPrivacyPolicyViewPager :
    BaseFragment<ViewPagerAsPrivacyPolicyBinding, AccountSetupViewModel>() {


    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        binding.textViewPolicyDesc.text = Html.fromHtml(
            viewUtil.getFileText(
                (activity as AccountSetupActivity),
                "as_privacy"
            )
        )

    }

    companion object {
        fun newInstance(): AsPrivacyPolicyViewPager {
            val fragment =
                AsPrivacyPolicyViewPager()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ViewPagerAsPrivacyPolicyBinding
        get() = ViewPagerAsPrivacyPolicyBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java

}