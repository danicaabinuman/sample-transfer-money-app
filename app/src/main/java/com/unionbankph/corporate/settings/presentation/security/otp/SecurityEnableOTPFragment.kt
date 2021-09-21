package com.unionbankph.corporate.settings.presentation.security.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ResultSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentSecurityEnableOtpBinding
import com.unionbankph.corporate.settings.data.form.OTPSettingsForm
import com.unionbankph.corporate.settings.data.model.OTPSettingsDto
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsDismissProgressBarLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsGetOTPSettings
import com.unionbankph.corporate.settings.presentation.ShowSettingsLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsOTPSettingsSuccess
import com.unionbankph.corporate.settings.presentation.ShowSettingsProgressBarLoading
import io.reactivex.rxkotlin.addTo

class SecurityEnableOTPFragment :
    BaseFragment<FragmentSecurityEnableOtpBinding, SettingsViewModel>() {

    private val dashboardActivity by lazyFast { (activity as DashboardActivity) }

    private var oTPSettingsDto: OTPSettingsDto = OTPSettingsDto()

    private var oTPSettingsForm: OTPSettingsForm = OTPSettingsForm()

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsLoading -> {
                    binding.viewLoadingState.viewLoadingLayout.visibility(true)
                    binding.constraintLayout.visibility(false)
                }
                is ShowSettingsDismissLoading -> {
                    binding.viewLoadingState.viewLoadingLayout.visibility(false)
                }
                is ShowSettingsProgressBarLoading -> {
                    showProgressAlertDialog(SecurityEnableOTPFragment::class.java.simpleName)
                }
                is ShowSettingsDismissProgressBarLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowSettingsGetOTPSettings -> {
                    binding.constraintLayout.visibility(true)
                    oTPSettingsDto = OTPSettingsDto(
                        it.otpSettingsDto.loginOtp,
                        it.otpSettingsDto.transactionOtp
                    )
                    oTPSettingsForm.loginOtp = oTPSettingsDto.loginOtp
                    oTPSettingsForm.transactionOtp = oTPSettingsDto.transactionOtp
                    initViews()
                }
                is ShowSettingsOTPSettingsSuccess -> {
                    val bundle = Bundle()
                    bundle.putString(
                        OTPActivity.EXTRA_REQUEST,
                        JsonHelper.toJson(it.auth)
                    )
                    bundle.putString(
                        OTPActivity.EXTRA_REQUEST_PAGE,
                        OTPActivity.PAGE_SECURITY
                    )
                    bundle.putString(
                        OTPActivity.EXTRA_SECURITY_FORM,
                        JsonHelper.toJson(oTPSettingsForm)
                    )
                    navigator.navigate(
                        (activity as DashboardActivity),
                        OTPActivity::class.java,
                        bundle,
                        isClear = false,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                }
                is ShowSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.getOTPSettings()
    }

    private fun initViews() {
        binding.switchLoginAuth.tag = SecurityEnableOTPFragment::class.java.simpleName
        binding.switchTransactionAuth.tag = SecurityEnableOTPFragment::class.java.simpleName

        binding.switchLoginAuth.isChecked = oTPSettingsDto.loginOtp
        binding.switchTransactionAuth.isChecked = oTPSettingsDto.transactionOtp
    }

    override fun onViewsBound() {
        super.onViewsBound()
        dashboardActivity.imageViewHelp().visibility = View.GONE
        dashboardActivity.setToolbarTitle(
            getString(R.string.title_enable_otp),
            hasBackButton = true,
            hasMenuItem = false
        )
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        eventBus.resultSyncEvent.flowable.subscribe {
            if (it.eventType == ResultSyncEvent.ACTION_SUCCESS_SECURITY) {
                this.oTPSettingsDto = JsonHelper.fromJson(it.payload)
                initViews()
            } else if (it.eventType == ResultSyncEvent.ACTION_BACK_SECURITY) {
                binding.switchLoginAuth.tag = SecurityEnableOTPFragment::class.java.simpleName
                binding.switchTransactionAuth.tag = SecurityEnableOTPFragment::class.java.simpleName
                initViews()
            }
        }.addTo(disposables)

        binding.switchLoginAuth.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchLoginAuth.tag != null) {
                binding.switchLoginAuth.tag = null
                return@setOnCheckedChangeListener
            }
            oTPSettingsForm.loginOtp = isChecked
            viewModel.oTPSettings(oTPSettingsForm)
        }

        binding.switchTransactionAuth.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchTransactionAuth.tag != null) {
                binding.switchTransactionAuth.tag = null
                return@setOnCheckedChangeListener
            }
            oTPSettingsForm.transactionOtp = isChecked
            viewModel.oTPSettings(oTPSettingsForm)
        }

        binding.switchLoginAuth.setOnTouchListener { view, motionEvent ->
            binding.switchLoginAuth.tag = null
            return@setOnTouchListener false
        }

        binding.switchTransactionAuth.setOnTouchListener { view, motionEvent ->
            binding.switchTransactionAuth.tag = null
            return@setOnTouchListener false
        }
    }

    override val viewModelClassType: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSecurityEnableOtpBinding
        get() = FragmentSecurityEnableOtpBinding::inflate
}
