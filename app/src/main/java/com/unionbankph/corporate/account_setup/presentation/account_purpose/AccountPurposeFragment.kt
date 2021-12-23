package com.unionbankph.corporate.account_setup.presentation.account_purpose

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentAccountPurposeBinding

class AccountPurposeFragment :
    BaseFragment<FragmentAccountPurposeBinding, AccountPurposeViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAccountPurposeBinding
        get() = FragmentAccountPurposeBinding::inflate
    override val viewModelClassType: Class<AccountPurposeViewModel>
        get() = AccountPurposeViewModel::class.java

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }


    private fun initViews() {

    }

    private fun initObservers() {

    }
}