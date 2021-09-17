package com.unionbankph.corporate.open_account.presentation.select_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.runPostDelayed
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentOaAccountSelectionBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel

class OAAccountSelection :
    BaseFragment<FragmentOaAccountSelectionBinding, OpenAccountViewModel>() {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        openAccountActivity.setIsScreenScrollable(false)
        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.clearCache()
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

    override fun onViewsBound() {
        super.onViewsBound()

        initClickListener()
    }

    private fun initClickListener() {
        binding.btnOpenAccount.setOnClickListener {
            findNavController().navigate(R.id.action_selection_to_reminder)
        }
        binding.btnContinueExistingAccountApplication.setOnClickListener {
            findNavController().navigate(R.id.action_dao_selection_activity)
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaAccountSelectionBinding
        get() = FragmentOaAccountSelectionBinding::inflate

    override val viewModelClassType: Class<OpenAccountViewModel>
        get() = OpenAccountViewModel::class.java
}