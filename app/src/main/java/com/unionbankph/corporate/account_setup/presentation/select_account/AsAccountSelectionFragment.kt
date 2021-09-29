package com.unionbankph.corporate.account_setup.presentation.select_account

import android.os.Bundle
import android.util.Log
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
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentAsAccountSelectionBinding
import io.reactivex.rxkotlin.addTo

class AsAccountSelectionFragment : BaseFragment<FragmentAsAccountSelectionBinding, AccountSetupViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.setIsScreenScrollable(false)
        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
           navigateDashboardScreen()
            return@addCallback
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
    }



    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.buttonNext.setOnClickListener { attemptSubmit() }

        binding.radioButtonIndividual.setOnClickListener {
            viewModel.setupAccountType.onNext(0)
            initRadioButtonListener(it.id)
        }
        binding.radioButtonSoleProp.setOnClickListener {
            viewModel.setupAccountType.onNext(1)
            initRadioButtonListener(it.id)
        }
        binding.radioButtonPartnerShip.setOnClickListener {
            viewModel.setupAccountType.onNext(2)
            initRadioButtonListener(it.id)
        }
        binding.radioButtonCorp.setOnClickListener {
            viewModel.setupAccountType.onNext(3)
            initRadioButtonListener(it.id)
        }

    }

    private fun initRadioButtonListener(checkedId: Int) {
        binding.constraintLayout.visibility(true)
        binding.buttonNext.enableButtonMSME(true)
        when(checkedId){
            R.id.radioButtonIndividual -> {
                binding.textViewAccountLabel.text = getString(R.string.title_individual)
                binding.textViewAccountDesc.text = getString(R.string.desc_sole_prop)
                binding.radioButtonSoleProp.isChecked = false
                binding.radioButtonPartnerShip.isChecked = false
                binding.radioButtonCorp.isChecked = false
            }
            R.id.radioButtonSoleProp -> {
                binding.textViewAccountLabel.text = getString(R.string.title_sole_proprietorship_msme)
                binding.textViewAccountDesc.text = getString(R.string.desc_sole_prop)
                binding.radioButtonIndividual.isChecked = false
                binding.radioButtonPartnerShip.isChecked = false
                binding.radioButtonCorp.isChecked = false
            }
            R.id.radioButtonPartnerShip -> {
                binding.textViewAccountLabel.text = getString(R.string.title_partnership)
                binding.textViewAccountDesc.text = getString(R.string.desc_partnership)
                binding.radioButtonIndividual.isChecked = false
                binding.radioButtonSoleProp.isChecked = false
                binding.radioButtonCorp.isChecked = false
            }
            R.id.radioButtonCorp -> {
                binding.textViewAccountLabel.text = getString(R.string.title_corporation)
                binding.textViewAccountDesc.text = getString(R.string.desc_corporation)
                binding.radioButtonIndividual.isChecked = false
                binding.radioButtonSoleProp.isChecked = false
                binding.radioButtonPartnerShip.isChecked = false
            }

        }
    }

    private fun attemptSubmit(){
        when(viewModel.setupAccountType.value){
            0 -> {}
            1 -> {}
            2 -> {}
            3 -> {}
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



    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsAccountSelectionBinding
        get() = FragmentAsAccountSelectionBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java
}