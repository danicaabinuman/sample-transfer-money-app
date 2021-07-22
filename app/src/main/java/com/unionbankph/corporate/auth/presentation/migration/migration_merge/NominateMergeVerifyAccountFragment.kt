package com.unionbankph.corporate.auth.presentation.migration.migration_merge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.widget.edittext.PincodeEditText
import com.unionbankph.corporate.auth.data.form.ECredMergeAccountOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.ECredMergeSubmitDto
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationECredMergeAccountOTP
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationOTPCompleteTimer
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationOTPTimer
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationResendOTPSuccess
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentNominateVerifyAccountBinding
import io.reactivex.rxkotlin.addTo

class NominateMergeVerifyAccountFragment :
    BaseFragment<FragmentNominateVerifyAccountBinding, MigrationViewModel>(),
    PincodeEditText.OnOTPCallback, MigrationMergeActivity.OnBackPressedEvent {

    private lateinit var pinCodeEditText: PincodeEditText

    private var auth = Auth()

    private val migrationMergeActivity by lazyFast { (activity as MigrationMergeActivity) }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationECredMergeAccountOTP -> {
                    disposables.clear()
                    disposables.dispose()
                    navigateMigrationMergeResult()
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
            binding.editTextHidden.requestFocus()
            if (!viewUtil.isSoftKeyboardShown(binding.parentLayout))
                viewUtil.showKeyboard(getAppCompatActivity())
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
    }

    override fun onSubmitOTP(otpCode: String) {
        if (migrationMergeActivity.getType() == MigrationMainActivity.TYPE_ECREDITING) {
            viewModel.mergeECredAccountOTP(
                migrationMergeActivity.getAccessToken(),
                ECredMergeAccountOTPForm(
                    auth.requestId,
                    pinCodeEditText.getPinCode(),
                    TYPE_SMS
                )
            )
        }
    }

    override fun onBackPressed() {
        initStartResendCodeCount(resources.getInteger(R.integer.resend_otp_code_count))
        viewModel.clearCountDownDisposable()
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_AUTH_MIGRATION_MERGE) {
                val eCredMergeSubmitDto = JsonHelper.fromJson<ECredMergeSubmitDto>(it.payload)
                auth.mobileNumber = eCredMergeSubmitDto.mobileNumber
                auth.requestId = eCredMergeSubmitDto.requestId
                updateScreen()
            }
        }.addTo(disposables)
    }

    private fun updateScreen() {
        binding.tvVerifyAccountDesc.text = formatString(
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

    private fun initClickListener() {
        binding.btnResend.setOnClickListener {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            viewModel.corpLoginResendOTPMigration(
                migrationMergeActivity.getAccessToken(),
                auth.requestId
            )
        }
        binding.parentLayout.setOnClickListener {
            viewUtil.dismissKeyboard(getAppCompatActivity())
        }
    }

    private fun navigateMigrationMergeResult() {
        migrationMergeActivity.getViewPager().currentItem =
            migrationMergeActivity.getViewPager().currentItem.plus(1)
    }

    private fun init() {
        migrationMergeActivity.setOnBackPressedEvent(this)
        pinCodeEditText =
            PincodeEditText(
                getAppCompatActivity(),
                binding.parentLayout,
                binding.btnSubmit,
                binding.editTextHidden,
                binding.viewPinCode.etPin1,
                binding.viewPinCode.etPin2,
                binding.viewPinCode.etPin3,
                binding.viewPinCode.etPin4,
                binding.viewPinCode.etPin5,
                binding.viewPinCode.etPin6
            )
        pinCodeEditText.setOnOTPCallback(this)
    }

    private fun initStartResendCodeCount(timer: Int) {
        binding.tvResend.text = formatString(
            R.string.desc_resend_code_seconds,
            formatString(
                R.string.param_color,
                convertColorResourceToHex(if (isSME) getAccentColor() else R.color.colorWhite),
                "$timer seconds"
            )
        ).toHtmlSpan()
    }

    private fun initEnableResendButton(isEnabled: Boolean) {
        binding.btnResend.isEnabled = isEnabled
        binding.btnResend.alpha = if (isEnabled) 1.0F else 0.5F
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
        fun newInstance(): NominateMergeVerifyAccountFragment {
            val fragment =
                NominateMergeVerifyAccountFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNominateVerifyAccountBinding
        get() = FragmentNominateVerifyAccountBinding::inflate
}
