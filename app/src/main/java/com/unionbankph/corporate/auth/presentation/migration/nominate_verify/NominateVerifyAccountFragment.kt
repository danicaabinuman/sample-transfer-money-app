package com.unionbankph.corporate.auth.presentation.migration.nominate_verify

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.edittext.PincodeEditText
import com.unionbankph.corporate.auth.data.form.ECredOTPForm
import com.unionbankph.corporate.auth.data.form.MigrationForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationOTPCompleteTimer
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationOTPTimer
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationResendOTPSuccess
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationVerify
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationVerifyECred
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_nominate_verify_account.*
import kotlinx.android.synthetic.main.widget_pin_code.*

class NominateVerifyAccountFragment :
    BaseFragment<MigrationViewModel>(R.layout.fragment_nominate_verify_account),
    PincodeEditText.OnOTPCallback, MigrationMainActivity.OnBackPressedEvent {

    private lateinit var pinCodeEditText: PincodeEditText

    private lateinit var auth: Auth

    private val migrationMainActivity by lazyFast { (activity as MigrationMainActivity) }

    private val loginMigrationDto by lazyFast { migrationMainActivity.getLoginMigrationInfo() }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MigrationViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(
                        NominateVerifyAccountFragment::class.java.simpleName
                    )
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationVerify -> {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_UPDATE_VERIFY_RESULT_MIGRATION,
                            JsonHelper.toJson(it.migrationSubmitDto)
                        )
                    )
                    navigateMigrationResult()
                }
                is ShowMigrationVerifyECred -> {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_UPDATE_VERIFY_RESULT_ECRED_MIGRATION,
                            JsonHelper.toJson(it.eCredSubmitOTPDto)
                        )
                    )
                    navigateMigrationResult()
                }
                is ShowMigrationResendOTPSuccess -> {
                    auth.requestId = it.auth.requestId
                    onSuccessResendOTP()
                }
                is ShowMigrationOTPTimer -> {
                    initStartResendCodeCount(it.timer)
                }
                is ShowMigrationOTPCompleteTimer -> {
                    initStartResendCodeCount(0)
                    initEnableResendButton(!it.isClickedResendOTP)
                }
                is ShowMigrationError -> {
                    pinCodeEditText.clearPinCode()
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            init()
        }
        if (!hasInitialLoad) {
            editTextHidden.requestFocus()
            if (!viewUtil.isSoftKeyboardShown(parentLayout))
                viewUtil.showKeyboard(getAppCompatActivity())
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
    }

    override fun onSubmitOTP(otpCode: String) {
        if (migrationMainActivity.getType() == MigrationMainActivity.TYPE_ECREDITING) {
            viewModel.nominateECredFormOTP(
                migrationMainActivity.getAccessToken(),
                ECredOTPForm(
                    auth.requestId,
                    pinCodeEditText.getPinCode(),
                    TYPE_SMS
                )
            )
        } else {
            viewModel.submitMigration(
                MigrationForm(
                    pinCodeEditText.getPinCode(),
                    loginMigrationDto.migrationToken,
                    auth.requestId
                )
            )
        }
    }

    override fun onBackPressed() {
        initStartResendCodeCount(resources.getInteger(R.integer.resend_otp_code_count))
        viewModel.clearCountDownDisposable()
    }

    private fun initClickListener() {
        btnResend.setOnClickListener {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            if (migrationMainActivity.getType() == MigrationMainActivity.TYPE_ECREDITING) {
                viewModel.updateDetailsResendOTPMigration(
                    migrationMainActivity.getAccessToken(),
                    auth.requestId
                )
            } else {
                viewModel.eBankingResendOTPMigration(
                    loginMigrationDto.temporaryCorporateUserId.notNullable(),
                    loginMigrationDto.migrationToken,
                    auth.requestId
                )
            }
        }
        parentLayout.setOnClickListener {
            viewUtil.dismissKeyboard(getAppCompatActivity())
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_AUTH_MIGRATION) {
                auth = JsonHelper.fromJson(it.payload)
                updateScreen()
            }
        }.addTo(disposables)
    }

    private fun updateScreen() {
        tvVerifyAccountDesc.text = formatString(
            R.string.desc_verify_account_sms,
            formatString(
                R.string.param_color,
                convertColorResourceToHex(if (isSME) getAccentColor() else R.color.colorWhite),
                auth.mobileNumber.notNullable()
            )
        ).toHtmlSpan()
        initStartResendCodeCount(resources.getInteger(R.integer.resend_otp_code_count))
        initEnableResendButton(false)
        viewModel.countDownTimer(
            resources.getInteger(R.integer.resend_otp_code_period).toLong(),
            resources.getInteger(R.integer.resend_otp_code_count).toLong()
        )
    }

    private fun navigateMigrationResult() {
        disposables.clear()
        disposables.dispose()
        migrationMainActivity.getViewPager().currentItem =
            migrationMainActivity.getViewPager().currentItem.plus(1)
    }

    private fun init() {
        migrationMainActivity.setOnBackPressedEvent(this)
        pinCodeEditText =
            PincodeEditText(
                getAppCompatActivity(),
                parentLayout,
                btnSubmit,
                editTextHidden,
                etPin1,
                etPin2,
                etPin3,
                etPin4,
                etPin5,
                etPin6
            )
        pinCodeEditText.setOnOTPCallback(this)
    }

    private fun initStartResendCodeCount(timer: Int) {
        tvResend.text = formatString(
            R.string.desc_resend_code_seconds,
            formatString(
                R.string.param_color,
                convertColorResourceToHex(if (isSME) getAccentColor() else R.color.colorWhite),
                "$timer seconds"
            )
        ).toHtmlSpan()
    }

    private fun initEnableResendButton(isEnabled: Boolean) {
        btnResend.isEnabled = isEnabled
        btnResend.alpha = if (isEnabled) 1.0F else 0.5F
    }

    private fun onSuccessResendOTP() {
        initEnableResendButton(false)
        pinCodeEditText.clearPinCode()
        MaterialDialog(getAppCompatActivity()).show {
            lifecycleOwner(getAppCompatActivity())
            title(R.string.title_code_resent)
            message(R.string.msg_resent_otp)
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                }
            )
        }
    }

    private fun isTOTPScreen(loginType: String?): Boolean = loginType == LOGIN_TYPE_TOTP

    companion object {
        fun newInstance(): NominateVerifyAccountFragment {
            val fragment =
                NominateVerifyAccountFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
