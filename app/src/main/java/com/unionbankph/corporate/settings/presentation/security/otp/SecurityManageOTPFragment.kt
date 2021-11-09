package com.unionbankph.corporate.settings.presentation.security.otp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.databinding.FragmentSecurityManageOtpBinding
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsViewModel
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsError
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsGetOTPType
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsLoading
import com.unionbankph.corporate.settings.presentation.general.ShowLoginHasTOTPAccess
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class SecurityManageOTPFragment :
    BaseFragment<FragmentSecurityManageOtpBinding, GeneralSettingsViewModel>() {

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralSettingsLoading -> {
                    showLoading()
                }
                is ShowGeneralSettingsDismissLoading -> {
                    dismissLoading()
                }
                is ShowGeneralSettingsError -> {
                    showContent(false)
                    handleOnError(it.throwable)
                }
                is ShowLoginHasTOTPAccess -> {
                    if (!it.isTrustedDevice) {
                        binding.constraintLayoutGenerateOTP.visibility(false)
                        binding.viewBorderBottom.visibility(false)
                    }
                }
                is ShowGeneralSettingsGetOTPType -> {
                    showContent(true)
                    setTextViewOTPType(it.otpTypeDto.loginType)
                }
            }
        })
        viewModel.getOTPTypes()
        viewModel.hasTOTP()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).btnHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_otp),
            hasBackButton = true,
            hasMenuItem = false
        )
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        RxView.clicks(binding.constraintLayoutEnableOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_ENABLE_OTP)
                )
            }.addTo(disposables)
        RxView.clicks(binding.constraintLayoutReceiveOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_RECEIVE_OTP)
                )
            }.addTo(disposables)

        RxView.clicks(binding.constraintLayoutGenerateOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_GENERATE_OTP)
                )
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_OTP_SETTINGS) {
                val isShowGenerateOTP = it.payload == "1"
                binding.constraintLayoutGenerateOTP.visibility(isShowGenerateOTP)
                binding.viewBorderBottom.visibility(isShowGenerateOTP)
            } else if (it.eventType == ActionSyncEvent.ACTION_UPDATE_RECEIVE_OTP) {
                setTextViewOTPType(it.payload)
            }
        }.addTo(disposables)
    }

    private fun showLoading() {
        binding.constraintLayoutContent.visibility(false)
        binding.viewLoadingState.viewLoadingLayout.visibility(true)
    }

    private fun dismissLoading() {
        binding.viewLoadingState.viewLoadingLayout.visibility(false)
    }

    private fun showContent(isShown: Boolean) {
        binding.constraintLayoutContent.visibility(isShown)
    }

    private fun setTextViewOTPType(type: String?) {
        binding.textViewReceiveOTP.text = formatString(
            R.string.msg_receive_otp,
            formatString(
                R.string.param_color,
                convertColorResourceToHex(getAccentColor()),
                if (type == TYPE_SMS) {
                    formatString(R.string.title_sms)
                } else {
                    formatString(R.string.title_trusted_device)
                }
            )
        ).toHtmlSpan()
    }

    override val viewModelClassType: Class<GeneralSettingsViewModel>
        get() = GeneralSettingsViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSecurityManageOtpBinding
        get() = FragmentSecurityManageOtpBinding::inflate
}
