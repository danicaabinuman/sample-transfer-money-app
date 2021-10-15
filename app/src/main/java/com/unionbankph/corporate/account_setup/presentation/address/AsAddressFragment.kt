package com.unionbankph.corporate.account_setup.presentation.address

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.databinding.FragmentAsAddressBinding

class AsAddressFragment : BaseFragment<FragmentAsAddressBinding, AccountSetupViewModel>(){



    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsAddressBinding
        get() = FragmentAsAddressBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}