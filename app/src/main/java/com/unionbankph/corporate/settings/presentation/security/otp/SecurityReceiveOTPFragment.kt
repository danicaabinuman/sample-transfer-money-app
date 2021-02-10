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
import com.unionbankph.corporate.settings.data.form.OTPTypeForm
import com.unionbankph.corporate.settings.data.model.OTPTypeDto
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsViewModel
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsError
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsGetOTPType
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsProgressDismissLoading
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsProgressLoading
import kotlinx.android.synthetic.main.fragment_security_receive_otp.*

class SecurityReceiveOTPFragment :
    BaseFragment<GeneralSettingsViewModel>(R.layout.fragment_security_receive_otp),
    View.OnClickListener {

    private lateinit var otpTypeDto: OTPTypeDto

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[GeneralSettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralSettingsLoading -> {
                    showLoading(
                        viewLoadingState,
                        null,
                        constraintLayoutContent,
                        null
                    )
                }
                is ShowGeneralSettingsDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        null,
                        constraintLayoutContent
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
                        imageViewSMS.visibility = View.VISIBLE
                        imageViewTrustedDevice.visibility = View.GONE
                    } else {
                        imageViewSMS.visibility = View.GONE
                        imageViewTrustedDevice.visibility = View.VISIBLE
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
        constraintLayoutSMS.tag = TYPE_SMS
        constraintLayoutTrustedDevice.tag = TYPE_TOTP
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        constraintLayoutSMS.setOnClickListener(this)
        constraintLayoutTrustedDevice.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        if (otpTypeDto.loginType != v.tag) {
            viewModel.setOTPType(
                OTPTypeForm(
                    if (imageViewSMS.visibility == View.VISIBLE) {
                        TYPE_TOTP
                    } else {
                        TYPE_SMS
                    }
                )
            )
        }
    }
}
