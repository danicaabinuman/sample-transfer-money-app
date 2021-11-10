package com.unionbankph.corporate.user_creation.presentation.select_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcAccountSelectionBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class UcAccountSelectionFragment :
    BaseFragment<FragmentUcAccountSelectionBinding, UcAccountSelectionViewModel>() {

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        userCreationActivity.setIsScreenScrollable(false)
        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            userCreationActivity.viewModel.clearCache()
            return@addCallback
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(viewLifecycleOwner, EventObserver {
            userCreationActivity.viewModel.setSelectedAccountType(it)

            when (it) {
                Constant.SelectedAccountType.NO_OPEN_ACCOUNT ->
                    userCreationActivity.navigateToWebApps(UserCreationActivity.WEB_APP_ACCOUNT_OPENING)
                Constant.SelectedAccountType.YES_UNIONBANK_ACCOUNT ->
                    userCreationActivity.navigateToWebApps(UserCreationActivity.WEB_APP_ENROLlMENT)
                Constant.SelectedAccountType.CONTINUE_EXISTING_ACCOUNT ->
                    findNavController().navigate(R.id.action_dao_activity)
            }
        })

        val selectedCitizenship = userCreationActivity.viewModel.selectedAccount
        selectedCitizenship?.let {
            viewModel.selectedAccountInput.onNext(it)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViewBindings()
    }

    private fun initViewBindings() {
        viewModel.selectedAccountInput.subscribe {
            setSelectedButton(it)
        }.addTo(disposables)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        initClickListener()
    }

    private fun initClickListener() {
        binding.btnOpenAccount.setOnClickListener { viewModel.noOpenAccount() }
        binding.btnExistingUbAccount.setOnClickListener { viewModel.yesUnionBankAccount() }
        binding.btnContinueExistingAccountApplication.setOnClickListener { viewModel.continueExistingAccount() }
    }

    private fun setSelectedButton(selectedAccountType: String) {
        binding.apply {
            btnOpenAccount.isChecked = selectedAccountType.equals(Constant.Citizenship.FILIPINO, true)
            btnExistingUbAccount.isChecked = selectedAccountType.equals(Constant.Citizenship.NON_FILIPINO, true)
            btnContinueExistingAccountApplication.isChecked = selectedAccountType.equals(Constant.Citizenship.NON_FILIPINO, true)
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcAccountSelectionBinding
        get() = FragmentUcAccountSelectionBinding::inflate

    override val viewModelClassType: Class<UcAccountSelectionViewModel>
        get() = UcAccountSelectionViewModel::class.java
}