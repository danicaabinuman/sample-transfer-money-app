package com.unionbankph.corporate.account_setup.presentation.terms_of_service

import android.view.LayoutInflater

import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.databinding.FragmentAsTermsOfServiceBinding
import timber.log.Timber

class AsTermsOfServiceFragment :
    BaseFragment<FragmentAsTermsOfServiceBinding, AccountSetupViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        binding.tvHotline.text = formatString(
            R.string.msg_terms_condition_74).toHtmlSpan()
        binding.tvBulleted.text = formatString(
            R.string.msg_terms_condition_76).toHtmlSpan()
        binding.svTerms.viewTreeObserver.addOnScrollChangedListener {
            val scrollY: Int = binding.svTerms.scrollY
            Timber.d("scrollY: $scrollY")
            if (scrollY > 0) {
                binding.btnProceed.enableButtonMSME(true)
            } else {
                binding.btnProceed.enableButtonMSME(false)
            }
        }
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsTermsOfServiceBinding
        get() = FragmentAsTermsOfServiceBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java

}