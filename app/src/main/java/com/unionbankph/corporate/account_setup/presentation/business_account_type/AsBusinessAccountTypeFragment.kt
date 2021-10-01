package com.unionbankph.corporate.account_setup.presentation.business_account_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.FragmentAsBusinessAccountTypeBinding


class AsBusinessAccountTypeFragment :
    BaseFragment<FragmentAsBusinessAccountTypeBinding, AccountSetupViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.setIsScreenScrollable(false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        updateUI(accountSetupActivity.getExistingBusinessAccountType())
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.buttonNext.setOnClickListener {
            findNavController().navigate(R.id.action_business_account_type_to_reminders)
        }
        binding.constraintLayoutStarter.setOnClickListener {
            onBusinessAccountTypeSelected(Constant.BusinessAccountType.STARTER)
        }
        binding.constraintLayoutCheck.setOnClickListener {
            onBusinessAccountTypeSelected(Constant.BusinessAccountType.CHECK)
        }
    }

    private fun onBusinessAccountTypeSelected(type: Int) {
        accountSetupActivity.setBusinessAccountType(type)
        updateUI(type)
    }

    private fun updateUI(type: Int) {
        binding.buttonNext.enableButton(type >= 0)

        when(type) {
            Constant.BusinessAccountType.STARTER -> {
                binding.constraintLayoutStarter.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_button_with_icon_selected
                )
                binding.constraintLayoutCheck.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_button_with_icon_default
                )
            }
            Constant.BusinessAccountType.CHECK -> {
                binding.constraintLayoutStarter.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_button_with_icon_default
                )
                binding.constraintLayoutCheck.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_button_with_icon_selected
                )
            }
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsBusinessAccountTypeBinding
        get() = FragmentAsBusinessAccountTypeBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}