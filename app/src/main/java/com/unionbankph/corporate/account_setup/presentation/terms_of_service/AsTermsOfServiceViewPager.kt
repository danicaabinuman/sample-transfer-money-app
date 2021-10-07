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
import com.unionbankph.corporate.databinding.ViewPagerAsTermsOfServiceBinding
import timber.log.Timber

class AsTermsOfServiceViewPager :
    BaseFragment<ViewPagerAsTermsOfServiceBinding, AccountSetupViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        binding.textViewTermsService.text = Html.fromHtml(
            viewUtil.getFileText(
                (activity as AccountSetupActivity),
                "as_terms"
            )
        )
            binding.scrollViewTermsService.viewTreeObserver.addOnScrollChangedListener {
                val scrollY: Int = binding.scrollViewTermsService.scrollY
                Timber.d("scrollY: $scrollY")
                if (scrollY  > 0) {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_SCROLL_TERMS_SERVICE,
                            "1"
                        )
                    )
                } else {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_SCROLL_TERMS_SERVICE,
                            "0"
                        )
                    )
                }
            }

    }


    companion object {
        fun newInstance(): AsTermsOfServiceViewPager {
            val fragment =
                AsTermsOfServiceViewPager()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ViewPagerAsTermsOfServiceBinding
        get() = ViewPagerAsTermsOfServiceBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java

}