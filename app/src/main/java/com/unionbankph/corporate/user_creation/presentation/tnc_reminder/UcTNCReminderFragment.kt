package com.unionbankph.corporate.user_creation.presentation.tnc_reminder

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcTncReminderBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class UcTNCReminderFragment :
    BaseFragment<FragmentUcTncReminderBinding, UcTNCReminderViewModel>() {

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        userCreationActivity.setIsScreenScrollable(false)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        initOnClicks()
    }

    private fun initViewModel() {

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

        val hasValue = userCreationActivity.viewModel.hasNameInput.hasValue()
        if (hasValue && !viewModel.isLoadedScreen.hasValue()) {
            userCreationActivity.viewModel.nameInput.let {
                viewModel.setName(it)
            }
        }

    }


    private fun initBinding() {
        Handler().post {
            viewModel.loadName(userCreationActivity.getDefaultForm())
            viewModel.input.firstNameInput
                .subscribe {
                    binding.txtWelcomeName.text = formatString(R.string.welcome_reminder, it)
                }.addTo(disposables)
        }
    }

    private fun initOnClicks(){
        RxView.clicks(binding.btnTncReminderNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_tnc_reminder_to_tnc)
            }.addTo(disposables)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcTncReminderBinding
        get() = FragmentUcTncReminderBinding::inflate

    override val viewModelClassType: Class<UcTNCReminderViewModel>
        get() = UcTNCReminderViewModel::class.java
}