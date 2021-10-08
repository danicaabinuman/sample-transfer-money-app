package com.unionbankph.corporate.account_setup.presentation.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
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
        accountSetupActivity.setToolbarButtonType(AccountSetupActivity.BUTTON_CLOSE)
        accountSetupActivity.showToolbarButton(true)
        accountSetupActivity.showProgress(true)
        accountSetupActivity.setProgressValue(3)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.buttonNext.setOnClickListener {
            onNextClicked()
        }
    }

    private fun onNextClicked() {
        findNavController().navigate(R.id.action_reminders_to_terms_of_service)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsRemindersBinding
        get() = FragmentAsRemindersBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}