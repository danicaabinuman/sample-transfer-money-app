package com.unionbankph.corporate.account_setup.presentation.business_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentAsBusinessTypeBinding

class AsBusinessTypeFragment
    : BaseFragment<FragmentAsBusinessTypeBinding, AccountSetupViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            showProgress(true)
            setProgressValue(1)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
                is UiState.Exit -> {
                    navigator.navigateClearStacks(
                        getAppCompatActivity(),
                        LoginActivity::class.java,
                        Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
                        true
                    )
                }
            }
        })

        updateUI(accountSetupActivity.getExistingBusinessType())
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.buttonNext.setOnClickListener { onNextClicked() }

        binding.radioButtonIndividual.setOnClickListener {
            onBusinessTypeSelected(Constant.BusinessType.INDIVIDUAL)
        }
        binding.radioButtonSoleProp.setOnClickListener {
            onBusinessTypeSelected(Constant.BusinessType.SOLE_PROP)
        }
        binding.radioButtonPartnerShip.setOnClickListener {
            onBusinessTypeSelected(Constant.BusinessType.PARTNERSHIP)
        }
        binding.radioButtonCorp.setOnClickListener {
            onBusinessTypeSelected(Constant.BusinessType.CORPORATION)
        }
    }

    private fun onBusinessTypeSelected(type: Int) {
        accountSetupActivity.setBusinessType(type)
        updateUI(type)
    }

    private fun updateUI(businessType: Int) {

//        if (businessType >= 0) {
//            binding.constraintLayout.slideDown()
//        }

        binding.constraintLayout.visibility(businessType >= 0)
        binding.buttonNext.enableButtonMSME(businessType >= 0)

        when(businessType){
            Constant.BusinessType.INDIVIDUAL -> {
                binding.textViewAccountLabel.text = getString(R.string.title_individual)
                binding.textViewAccountDesc.text = getString(R.string.desc_individual)
                binding.radioButtonSoleProp.isChecked = false
                binding.radioButtonPartnerShip.isChecked = false
                binding.radioButtonCorp.isChecked = false
            }
            Constant.BusinessType.SOLE_PROP -> {
                binding.textViewAccountLabel.text = getString(R.string.title_sole_proprietorship_msme)
                binding.textViewAccountDesc.text = getString(R.string.desc_sole_prop)
                binding.radioButtonIndividual.isChecked = false
                binding.radioButtonPartnerShip.isChecked = false
                binding.radioButtonCorp.isChecked = false
            }
            Constant.BusinessType.PARTNERSHIP -> {
                binding.textViewAccountLabel.text = getString(R.string.title_partnership)
                binding.textViewAccountDesc.text = getString(R.string.desc_partnership)
                binding.radioButtonIndividual.isChecked = false
                binding.radioButtonSoleProp.isChecked = false
                binding.radioButtonCorp.isChecked = false
            }
            Constant.BusinessType.CORPORATION -> {
                binding.textViewAccountLabel.text = getString(R.string.title_corporation)
                binding.textViewAccountDesc.text = getString(R.string.desc_corporation)
                binding.radioButtonIndividual.isChecked = false
                binding.radioButtonSoleProp.isChecked = false
                binding.radioButtonPartnerShip.isChecked = false
            }
        }
    }

    private fun onNextClicked() {
        when (accountSetupActivity.getExistingBusinessType()) {
            Constant.BusinessType.INDIVIDUAL -> findNavController().navigate(R.id.action_business_type_to_debit_card_type)
            else-> findNavController().navigate(R.id.action_business_type_to_account_type)
        }

    }

    private fun navigateDashboardScreen() {
        val bundle = Bundle().apply {
            putString(
                AutobahnFirebaseMessagingService.EXTRA_DATA,
                getAppCompatActivity().intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
            )
        }
        navigator.navigateClearStacks(
            getAppCompatActivity(),
            DashboardActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsBusinessTypeBinding
        get() = FragmentAsBusinessTypeBinding::inflate

    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}