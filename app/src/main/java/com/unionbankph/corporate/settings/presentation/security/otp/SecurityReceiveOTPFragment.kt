package com.unionbankph.corporate.settings.presentation.security.otp

import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.TYPE_SMS
import com.unionbankph.corporate.app.common.extension.TYPE_TOTP
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.databinding.FragmentSecurityReceiveOtpBinding
import com.unionbankph.corporate.settings.data.form.OTPTypeForm
import com.unionbankph.corporate.settings.data.model.OTPTypeDto
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsViewModel
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsError
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsGetOTPType
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsProgressDismissLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsProgressLoading

class SecurityReceiveOTPFragment :
    BaseFragment<FragmentSecurityReceiveOtpBinding, GeneralSettingsViewModel>(),
    View.OnClickListener {

    private lateinit var otpTypeDto: OTPTypeDto

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralSettingsLoading -> {
                    showLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        null,
                        binding.constraintLayoutContent,
                        null
                    )
                }
                is ShowGeneralSettingsDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        null,
                        binding.constraintLayoutContent
                    )
                }
                is ShowGeneralSettingsProgressLoading -> {
                    showProgressAlertDialog(
                        SecurityReceiveOTPFragment::class.java.simpleName
                    )
                }
                is ShowGeneralSettingsProgressDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowGeneralSettingsGetOTPType -> {
                    otpTypeDto = it.otpTypeDto
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_UPDATE_RECEIVE_OTP,
                            otpTypeDto.loginType.notNullable()
                        )
                    )
                    if (otpTypeDto.loginType == TYPE_SMS) {
                        binding.imageViewSMS.visibility = View.VISIBLE
                        binding.imageViewTrustedDevice.visibility = View.GONE
                    } else {
                        binding.imageViewSMS.visibility = View.GONE
                        binding.imageViewTrustedDevice.visibility = View.VISIBLE
                    }
                }
                is ShowGeneralSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.getOTPTypes()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_receive_otp),
            hasBackButton = true,
            hasMenuItem = false
        )
        binding.constraintLayoutSMS.tag = TYPE_SMS
        binding.constraintLayoutTrustedDevice.tag = TYPE_TOTP
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.constraintLayoutSMS.setOnClickListener(this)
        binding.constraintLayoutTrustedDevice.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        if (otpTypeDto.loginType != v.tag) {
            viewModel.setOTPType(
                OTPTypeForm(
                    if (binding.imageViewSMS.visibility == View.VISIBLE) {
                        TYPE_TOTP
                    } else {
                        TYPE_SMS
                    }
                )
            )
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_security_receive_otp

    override val viewModelClassType: Class<GeneralSettingsViewModel>
        get() = GeneralSettingsViewModel::class.java
}
