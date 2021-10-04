package com.unionbankph.corporate.account_setup.presentation.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentAsRemindersBinding

class AsRemindersFragment
    : BaseFragment<FragmentAsRemindersBinding, AccountSetupViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.setIsScreenScrollable(false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.buttonNext.setOnClickListener {

        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsRemindersBinding
        get() = FragmentAsRemindersBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}