package com.unionbankph.corporate.account_setup.presentation.citizenship.non_filipino

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentAsNonFilipinoCitizenBinding

class AsNonFilipinoCitizenFragment :
    BaseFragment<FragmentAsNonFilipinoCitizenBinding, AsNonFilipinoCitizenViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.setBackButtonIcon(AccountSetupActivity.BACK_X_ICON)
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsNonFilipinoCitizenBinding
        get() = FragmentAsNonFilipinoCitizenBinding::inflate

    override val viewModelClassType: Class<AsNonFilipinoCitizenViewModel>
        get() = AsNonFilipinoCitizenViewModel::class.java
}