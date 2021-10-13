package com.unionbankph.corporate.account_setup.presentation.personal_info

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.databinding.FragmentAsPersonalInformationBinding

class AsPersonalInformationFragment
    : BaseFragment<FragmentAsPersonalInformationBinding, AccountSetupViewModel>(){



    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsPersonalInformationBinding
        get() = FragmentAsPersonalInformationBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}