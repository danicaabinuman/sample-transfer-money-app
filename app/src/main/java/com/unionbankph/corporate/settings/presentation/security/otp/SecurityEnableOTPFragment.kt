package com.unionbankph.corporate.settings.presentation.security.otp

import android.os.Bundle
import android.view.View
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
import kotlinx.android.synthetic.main.fragment_security_enable_otp.*

class SecurityEnableOTPFragment :
    BaseFragment<SettingsViewModel>(R.layout.fragment_security_enable_otp) {

    private val dashboardActivity by lazyFast { (activity as DashboardActivity) }

    private var oTPSettingsDto: OTPSettingsDto = OTPSettingsDto()

    private var oTPSettingsForm: OTPSettingsForm = OTPSettingsForm()

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsLoading -> {
                    viewLoadingState.visibility(true)
                    constraintLayout.visibility(false)
                }
                is ShowSettingsDismissLoading -> {
                    viewLoadingState.visibility(false)
                }
                is ShowSettingsProgressBarLoading -> {
                    showProgressAlertDialog(SecurityEnableOTPFragment::class.java.simpleName)
                }
                is ShowSettingsDismissProgressBarLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowSettingsGetOTPSettings -> {
                    constraintLayout.visibility(true)
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
        switchLoginAuth.tag = SecurityEnableOTPFragment::class.java.simpleName
        switchTransactionAuth.tag = SecurityEnableOTPFragment::class.java.simpleName

        switchLoginAuth.isChecked = oTPSettingsDto.loginOtp
        switchTransactionAuth.isChecked = oTPSettingsDto.transactionOtp
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
                switchLoginAuth.tag = SecurityEnableOTPFragment::class.java.simpleName
                switchTransactionAuth.tag = SecurityEnableOTPFragment::class.java.simpleName
                initViews()
            }
        }.addTo(disposables)

        switchLoginAuth.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchLoginAuth.tag != null) {
                switchLoginAuth.tag = null
                return@setOnCheckedChangeListener
            }
            oTPSettingsForm.loginOtp = isChecked
            viewModel.oTPSettings(oTPSettingsForm)
        }

        switchTransactionAuth.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchTransactionAuth.tag != null) {
                switchTransactionAuth.tag = null
                return@setOnCheckedChangeListener
            }
            oTPSettingsForm.transactionOtp = isChecked
            viewModel.oTPSettings(oTPSettingsForm)
        }

        switchLoginAuth.setOnTouchListener { view, motionEvent ->
            switchLoginAuth.tag = null
            return@setOnTouchListener false
        }

        switchTransactionAuth.setOnTouchListener { view, motionEvent ->
            switchTransactionAuth.tag = null
            return@setOnTouchListener false
        }
    }
}
