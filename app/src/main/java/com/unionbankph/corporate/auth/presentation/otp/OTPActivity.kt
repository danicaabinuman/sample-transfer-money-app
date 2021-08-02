package com.unionbankph.corporate.auth.presentation.otp

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.appbar.AppBarLayout
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ResultSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.edittext.PincodeEditText
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.receiver.SMSReceiver
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.auth.data.form.*
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.ECredLoginDto
import com.unionbankph.corporate.auth.data.model.ECredLoginOTPDto
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_summary.BillsPaymentSummaryActivity
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPaySummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.pddts.PDDTSSummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.pesonet.PesoNetSummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift.SwiftSummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPSummaryActivity
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.widget_pin_code.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Created by Herald Santos
 */
class OTPActivity :
    BaseActivity<OTPViewModel>(R.layout.activity_otp),
    PincodeEditText.OnOTPCallback {

    private val page by lazyFast { intent.getStringExtra(EXTRA_REQUEST_PAGE) }

    private lateinit var pinCodeEditText: PincodeEditText

    private lateinit var auth: Auth

    private var accountType: String? = null

    private var smsBroadcastReceiver: SMSReceiver? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
        (viewToolbar as AppBarLayout).isLiftOnScroll = true
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[OTPViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowOTPLoading -> {
                    showProgressAlertDialog(OTPActivity::class.java.simpleName)
                }
                is ShowOTPResendLoading -> {
                    showProgressAlertDialog(OTPActivity::class.java.simpleName)
                }
                is ShowOTPDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowOTPCompleteTimer -> {
                    initStartResendCodeCount(0)
                    initEnableResendButton(!it.isClickedResendOTP)
                }
                is ShowOTPTimer -> {
                    initStartResendCodeCount(it.timer)
                }
                is ShowOTPSuccessResend -> {
                    initSuccessResendOTP(it)
                }
                is ShowOTPLoginSuccess -> {
                    initSuccessAccountVerify(it.privacyAgreed)
                }
                is ShowOTPLoginMigrationSuccess -> {
                    initOTPLoginMigrationSuccess(it.eCredLoginOTPDto)
                }
                is ShowOTPSettingsVerifySuccess -> {
                    initSuccessOTPSettings(it)
                }
                is ShowOTPPasswordSuccess -> {
                    initResetPasswordSuccess(it.message)
                }
                is ShowOTPSuccessFundTransfer -> {
                    initVerifyTransactionSuccess(it)
                }
                is ShowOTPSuccessBillsPayment -> {
                    initVerifyBillsPaymentSuccess(it)
                }
                is ShowOTPNominatePasswordError -> {
                    initNominatePasswordError(JsonHelper.toJson(it.apiError))
                }
                is ShowOTPError -> {
                    pinCodeEditText.clearPinCode()
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        startSMSUserConsent()

        RxView.clicks(buttonGenerateOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            ).subscribe {
                onClickedResendOTP()
            }.addTo(disposables)
        RxView.clicks(btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                submitTransaction()
            }.addTo(disposables)
        RxView.clicks(btnResend)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                onClickedResendOTP()
            }.addTo(disposables)
    }

    private fun startSMSUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null).addOnSuccessListener {
            Timber.d("Initialize SMS Consent Retriever")
        }.addOnFailureListener {
            Timber.d("Failed SMS Consent Retriever initialization ${it.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        initSMSReceiver()
    }

    private fun initSMSReceiver() {
        smsBroadcastReceiver = SMSReceiver()
        smsBroadcastReceiver?.smsBroadcastReceiverListener =
            object : SMSReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    startActivityForResult(intent, REQ_USER_CONSENT)
                }

                override fun onFailure() {

                }
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                viewUtil.dismissKeyboard(this)
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                getOTPFromMessage(message)
            }
        }
    }

    private fun getOTPFromMessage(message: String?) {
        // This will match any 6 digit number in the message
        // This will match any 6 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {
            editTextHidden.setText(matcher.group(0))
        }
    }

    override fun onSubmitOTP(otpCode: String) {
        submitTransaction()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val v = currentFocus
        if ((ev.action == MotionEvent.ACTION_UP ||
                    ev.action == MotionEvent.ACTION_MOVE) &&
            v is EditText &&
            !v.javaClass.name.startsWith(
                "android.webkit."
            )
        ) {
            val coordinate = IntArray(2)
            v.getLocationOnScreen(coordinate)
            val x = ev.rawX + v.getLeft() - coordinate[0]
            val y = ev.rawY + v.getTop() - coordinate[1]

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                viewUtil.dismissKeyboard(this)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (page == PAGE_SECURITY)
            eventBus.resultSyncEvent.emmit(
                BaseEvent(
                    ResultSyncEvent.ACTION_BACK_SECURITY,
                    intent.getStringExtra(EXTRA_SECURITY_FORM).notNullable()
                )
            )
        viewUtil.dismissKeyboard(this)
        super.onBackPressed()
    }

    private fun init() {
        auth = if (page == PAGE_MIGRATION) {
            val eCredLoginDto = JsonHelper.fromJson<ECredLoginDto>(
                intent.getStringExtra(EXTRA_REQUEST)
            )
            Auth(
                requestId = eCredLoginDto.requestId,
                mobileNumber = eCredLoginDto.mobileNumber,
                validity = eCredLoginDto.validity
            )
        } else {
            JsonHelper.fromJson(intent.getStringExtra(EXTRA_REQUEST))
        }
        Timber.d("auth: %s", auth)
        pinCodeEditText =
            PincodeEditText(
                this,
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
        when (page) {
            PAGE_LOGIN,
            PAGE_FUND_TRANSFER_UBP,
            PAGE_FUND_TRANSFER_PDDTS,
            PAGE_FUND_TRANSFER_PESONET,
            PAGE_FUND_TRANSFER_INSTAPAY,
            PAGE_FUND_TRANSFER_SWIFT,
            PAGE_BILLS_PAYMENT -> {
                switchScreen(auth.otpType.notNullable())
            }
            else -> {
                switchScreen(LOGIN_TYPE_SMS)
            }
        }
    }

    private fun onClickedResendOTP() {
        when (page) {
            PAGE_LOGIN -> {
                viewModel.resendOTPLogin(ResendOTPForm(auth.requestId))
            }
            PAGE_PASSWORD_RECOVERY -> {
                viewModel.resetPassResendOTP(ResetPasswordResendOTPForm(auth.requestId))
            }
            PAGE_CHANGE_MOBILE_NUMBER -> {
                viewModel.changeMobileNumberResendOTP(ResendOTPForm(auth.requestId))
            }
            PAGE_SECURITY -> {
                viewModel.oTPSettingsVerifyResend(ResendOTPForm(auth.requestId))
            }
            PAGE_NOMINATE_PASSWORD -> {
                viewModel.nominatePasswordResendOTP(NominatePasswordResendOTPForm(auth.requestId))
            }
            PAGE_MIGRATION -> {
                viewModel.loginResendOTPMigration(
                    intent.getStringExtra(EXTRA_REQUEST).notNullable()
                )
            }
            PAGE_BILLS_PAYMENT -> {
                viewModel.resendOTPBillsPayment(ResendOTPForm(auth.requestId))
            }
            PAGE_FUND_TRANSFER_UBP,
            PAGE_FUND_TRANSFER_PESONET,
            PAGE_FUND_TRANSFER_PDDTS,
            PAGE_FUND_TRANSFER_INSTAPAY,
            PAGE_FUND_TRANSFER_SWIFT -> {
                viewModel.resendOTPFundTransfer(ResendOTPForm(auth.requestId))
            }
        }
    }

    private fun initVerifyBillsPaymentSuccess(it: ShowOTPSuccessBillsPayment) {
        val bundle = Bundle()
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_BILLS_PAYMENT,
            intent.getStringExtra(EXTRA_FUND_TRANSFER_REQUEST)
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_QR_CONTENT,
            it.billsPaymentVerify.qrContent
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_ACCOUNT,
            intent.getStringExtra(EXTRA_SELECTED_ACCOUNT)
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_ACCOUNT_TYPE,
            accountType
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_STATUS,
            it.billsPaymentVerify.transactions[0].status?.type
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_REFERENCE_ID,
            it.billsPaymentVerify.transactions[0].tranId
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_APPROVAL_NAME,
            it.billsPaymentVerify.approvalHierarchy
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_ERROR_MESSAGE,
            it.billsPaymentVerify.transactions[0].errorMessage
        )
        bundle.putString(
            BillsPaymentSummaryActivity.EXTRA_BILLS_PAYMENT_DTO,
            JsonHelper.toJson(it.billsPaymentVerify)
        )
        navigator.navigate(
            this,
            BillsPaymentSummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initVerifyTransactionSuccess(it: ShowOTPSuccessFundTransfer) {
        accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE)
        when (page) {
            PAGE_FUND_TRANSFER_UBP -> {
                navigateUBPSummaryScreen(it)
            }
            PAGE_FUND_TRANSFER_PESONET -> {
                navigatePesonetSummaryScreen(it)
            }
            PAGE_FUND_TRANSFER_INSTAPAY -> {
                navigateInstapaySummaryScreen(it)
            }
            PAGE_FUND_TRANSFER_PDDTS -> {
                navigatePDDTSSummaryScreen(it)
            }
            PAGE_FUND_TRANSFER_SWIFT -> {
                navigateSwiftSummaryScreen(it)
            }
        }
    }

    private fun navigateSwiftSummaryScreen(it: ShowOTPSuccessFundTransfer) {
        val bundle = Bundle()
        bundle.putString(
            SwiftSummaryActivity.EXTRA_FUND_TRANSFER,
            intent.getStringExtra(EXTRA_FUND_TRANSFER_REQUEST)
        )
        bundle.putString(
            SwiftSummaryActivity.EXTRA_FUND_TRANSFER_DTO,
            JsonHelper.toJson(it.fundTransferVerify)
        )
        bundle.putString(
            SwiftSummaryActivity.EXTRA_ACCOUNT,
            intent.getStringExtra(EXTRA_SELECTED_ACCOUNT)
        )
        bundle.putString(SwiftSummaryActivity.EXTRA_ACCOUNT_TYPE, accountType)
        bundle.putString(
            SwiftSummaryActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            SwiftSummaryActivity.EXTRA_CUSTOM_SERVICE_FEE,
            intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            SwiftSummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigatePDDTSSummaryScreen(it: ShowOTPSuccessFundTransfer) {
        val bundle = Bundle()
        bundle.putString(
            PDDTSSummaryActivity.EXTRA_FUND_TRANSFER,
            intent.getStringExtra(EXTRA_FUND_TRANSFER_REQUEST)
        )
        bundle.putString(
            PDDTSSummaryActivity.EXTRA_FUND_TRANSFER_DTO,
            JsonHelper.toJson(it.fundTransferVerify)
        )
        bundle.putString(
            PesoNetSummaryActivity.EXTRA_ACCOUNT,
            intent.getStringExtra(EXTRA_SELECTED_ACCOUNT)
        )
        bundle.putString(PDDTSSummaryActivity.EXTRA_ACCOUNT_TYPE, accountType)
        bundle.putString(
            PDDTSSummaryActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            PDDTSSummaryActivity.EXTRA_CUSTOM_SERVICE_FEE,
            intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            PDDTSSummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateInstapaySummaryScreen(it: ShowOTPSuccessFundTransfer) {
        val bundle = Bundle()
        bundle.putString(
            InstaPaySummaryActivity.EXTRA_FUND_TRANSFER,
            intent.getStringExtra(EXTRA_FUND_TRANSFER_REQUEST)
        )
        bundle.putString(
            InstaPaySummaryActivity.EXTRA_FUND_TRANSFER_DTO,
            JsonHelper.toJson(it.fundTransferVerify)
        )
        bundle.putString(
            InstaPaySummaryActivity.EXTRA_ACCOUNT,
            intent.getStringExtra(EXTRA_SELECTED_ACCOUNT)
        )
        bundle.putString(
            InstaPaySummaryActivity.EXTRA_ACCOUNT_TYPE,
            accountType
        )
        bundle.putString(
            InstaPaySummaryActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            InstaPaySummaryActivity.EXTRA_CUSTOM_SERVICE_FEE,
            intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            InstaPaySummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigatePesonetSummaryScreen(it: ShowOTPSuccessFundTransfer) {
        val bundle = Bundle()
        bundle.putString(
            PesoNetSummaryActivity.EXTRA_FUND_TRANSFER,
            intent.getStringExtra(EXTRA_FUND_TRANSFER_REQUEST)
        )
        bundle.putString(
            PesoNetSummaryActivity.EXTRA_FUND_TRANSFER_DTO,
            JsonHelper.toJson(it.fundTransferVerify)
        )
        bundle.putString(
            PesoNetSummaryActivity.EXTRA_ACCOUNT,
            intent.getStringExtra(EXTRA_SELECTED_ACCOUNT)
        )
        bundle.putString(PesoNetSummaryActivity.EXTRA_ACCOUNT_TYPE, accountType)
        bundle.putString(
            PesoNetSummaryActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            PesoNetSummaryActivity.EXTRA_CUSTOM_SERVICE_FEE,
            intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            PesoNetSummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateUBPSummaryScreen(it: ShowOTPSuccessFundTransfer) {
        val bundle = Bundle()
        bundle.putString(
            UBPSummaryActivity.EXTRA_FUND_TRANSFER,
            intent.getStringExtra(EXTRA_FUND_TRANSFER_REQUEST)
        )
        bundle.putString(
            UBPSummaryActivity.EXTRA_FUND_TRANSFER_DTO,
            JsonHelper.toJson(it.fundTransferVerify)
        )
        bundle.putString(
            UBPSummaryActivity.EXTRA_ACCOUNT,
            intent.getStringExtra(EXTRA_SELECTED_ACCOUNT)
        )
        bundle.putString(UBPSummaryActivity.EXTRA_ACCOUNT_TYPE, accountType)
        bundle.putString(
            UBPSummaryActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            UBPSummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initResetPasswordSuccess(message: Message) {
        val bundle = Bundle()
        when (page) {
            PAGE_NOMINATE_PASSWORD -> {
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_PAGE,
                    ResultLandingPageActivity.PAGE_NOMINATE_PASSWORD
                )
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_TITLE,
                    getString(R.string.title_account_activation_successful)
                )
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_DESC,
                    message.message
                )
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_BUTTON,
                    getString(R.string.action_login)
                )
            }
            PAGE_PASSWORD_RECOVERY -> {
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_PAGE,
                    ResultLandingPageActivity.PAGE_RESET_PASSWORD
                )
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_TITLE,
                    getString(R.string.title_link_sent)
                )
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_DESC,
                    message.message
                )
                bundle.putString(
                    ResultLandingPageActivity.EXTRA_BUTTON,
                    getString(R.string.action_close)
                )
            }
        }
        navigator.navigateClearStacks(
            this,
            ResultLandingPageActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initNominatePasswordError(data: String?) {
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            ResultLandingPageActivity.PAGE_NOMINATE_PASSWORD
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DATA,
            data
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_BUTTON,
            getString(R.string.action_login)
        )
        navigator.navigateClearStacks(
            this,
            ResultLandingPageActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initSuccessOTPSettings(it: ShowOTPSettingsVerifySuccess) {
        eventBus.resultSyncEvent.emmit(
            BaseEvent(
                ResultSyncEvent.ACTION_SUCCESS_SECURITY,
                JsonHelper.toJson(it.otpSettingsDto)
            )
        )
        viewUtil.dismissKeyboard(this)
        super.onBackPressed()
    }

    private fun initSuccessAccountVerify(privacyAgreed: Boolean) {
        when (page) {
            PAGE_LOGIN -> {
                if (privacyAgreed) {
                    navigateDashboardScreen()
                } else {
                    navigatePrivacyPolicyScreen()
                }
            }
            PAGE_CHANGE_MOBILE_NUMBER -> {
                navigateResultLandingScreen(
                    getString(R.string.title_successfully_changed_mobile_number),
                    getString(R.string.msg_mobile_number_changed),
                    getString(R.string.action_close)
                )
            }
        }
    }

    private fun initOTPLoginMigrationSuccess(eCredLoginOTPDto: ECredLoginOTPDto) {
        val bundle = Bundle().apply {
            putString(
                MigrationMainActivity.EXTRA_DATA,
                JsonHelper.toJson(eCredLoginOTPDto)
            )
            putString(
                MigrationMainActivity.EXTRA_TYPE,
                MigrationMainActivity.TYPE_ECREDITING
            )
            putString(
                MigrationMainActivity.EXTRA_USER_ID,
                intent.getStringExtra(EXTRA_USER_ID)
            )
        }
        navigator.navigate(
            this,
            MigrationMainActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateResultLandingScreen(title: String, desc: String, button: String) {
        val bundle = Bundle().apply {
            putString(
                ResultLandingPageActivity.EXTRA_PAGE,
                ResultLandingPageActivity.PAGE_CHANGE_MOBILE_NUMBER
            )
            putString(ResultLandingPageActivity.EXTRA_TITLE, title)
            putString(ResultLandingPageActivity.EXTRA_DESC, desc)
            putString(ResultLandingPageActivity.EXTRA_BUTTON, button)
        }
        navigator.navigate(
            this,
            ResultLandingPageActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initSuccessResendOTP(it: ShowOTPSuccessResend) {
        auth.requestId = it.auth.requestId
        if (isTOTPScreen(auth.otpType)) {
            auth.otpType = LOGIN_TYPE_SMS
            switchScreen(LOGIN_TYPE_SMS)
            viewUtil.showKeyboard(this)
        } else {
            auth = it.auth
            initEnableResendButton(false)
            pinCodeEditText.clearPinCode()
            initStartResendCodeCount(resources.getInteger(R.integer.resend_otp_code_count_30))
            initEnableResendButton(false)
            viewModel.countDownTimer(
                resources.getInteger(R.integer.resend_otp_code_period).toLong(),
                resources.getInteger(R.integer.resend_otp_code_count_30).toLong()
            )
            MaterialDialog(this).show {
                lifecycleOwner(this@OTPActivity)
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

        btnResend.visibility = View.VISIBLE
        buttonGenerateOTP.visibility = View.GONE
    }

    private fun submitTransaction() {
        when (page) {
            PAGE_LOGIN -> {
                viewModel.userOTP(
                    LoginOTPForm(
                        requestId = auth.requestId,
                        code = pinCodeEditText.getPinCode(),
                        otpType = auth.otpType
                    )
                )
            }
            PAGE_CHANGE_MOBILE_NUMBER -> {
                val countryId = intent.getStringExtra(EXTRA_COUNTRY_ID).notNullable()
                val mobileNumber = intent.getStringExtra(EXTRA_MOBILE_NUMBER).notNullable()
                viewModel.userChangeMobileNumberOTP(
                    countryId,
                    mobileNumber,
                    auth.requestId.notNullable(),
                    pinCodeEditText.getPinCode()
                )
            }
            PAGE_FUND_TRANSFER_UBP,
            PAGE_FUND_TRANSFER_PESONET,
            PAGE_FUND_TRANSFER_PDDTS,
            PAGE_FUND_TRANSFER_INSTAPAY,
            PAGE_FUND_TRANSFER_SWIFT ->
                viewModel.fundTransferOTP(
                    auth.requestId.notNullable(),
                    pinCodeEditText.getPinCode(),
                    auth.otpType.notNullable()
                )
            PAGE_PASSWORD_RECOVERY -> viewModel.resetPassOTP(
                ResetPasswordOTPForm(
                    requestId = auth.requestId,
                    code = pinCodeEditText.getPinCode()
                )
            )
            PAGE_BILLS_PAYMENT -> viewModel.billsPaymentOTP(
                auth.requestId.notNullable(),
                pinCodeEditText.getPinCode()
            )
            PAGE_NOMINATE_PASSWORD -> viewModel.nominatePasswordOTP(
                NominatePasswordOTPForm(
                    requestId = auth.requestId,
                    code = pinCodeEditText.getPinCode()
                )
            )
            PAGE_SECURITY -> viewModel.oTPSettingsVerify(
                VerifyOTPForm(
                    auth.requestId,
                    pinCodeEditText.getPinCode()
                )
            )
            PAGE_MIGRATION -> {
                viewModel.loginMigrationOTP(
                    intent.getStringExtra(EXTRA_REQUEST).notNullable(),
                    pinCodeEditText.getPinCode()
                )
            }
        }
    }

    private fun initStartResendCodeCount(seconds: Int) {
        val minutesDisplay = (seconds / 60).toString().padStart(2, '0')
        val secondsDisplay = (seconds % 60).toString().padStart(2, '0')

        tvResendTimer.text = "$minutesDisplay:$secondsDisplay"
    }

    private fun initEnableResendButton(isEnabled: Boolean) {
        btnResend.isEnabled = isEnabled
        btnResend.setTextColor(
            if (isEnabled)
                ContextCompat.getColor(this, R.color.dsColorMediumOrange)
            else
                ContextCompat.getColor(this, R.color.dsColorMediumGray)
        )
    }

    private fun navigateDashboardScreen() {
        val bundle = Bundle()
        bundle.putString(
            AutobahnFirebaseMessagingService.EXTRA_DATA,
            intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        navigator.navigateClearStacks(
            this,
            DashboardActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigatePrivacyPolicyScreen() {
        val bundle = Bundle()
        bundle.putString(
            PrivacyPolicyActivity.EXTRA_REQUEST_PAGE,
            PrivacyPolicyActivity.PAGE_LOGIN
        )
        bundle.putString(
            AutobahnFirebaseMessagingService.EXTRA_DATA,
            intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        bundle.putString(PrivacyPolicyActivity.EXTRA_EMAIL, intent.getStringExtra(EXTRA_EMAIL))
        navigator.navigate(
            this,
            PrivacyPolicyActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun switchScreen(loginType: String?) {
        pinCodeEditText.clearPinCode()
        viewModel.otpType.onNext(auth.otpType ?: LOGIN_TYPE_SMS)
        if (isTOTPScreen(loginType)) {
            initEnableResendButton(true)
            textViewDidNotReceived.text = formatString(R.string.desc_cannot_generate_code)
            textViewDidNotReceived.visibility(true)
            tvResendTimer.visibility(false)
            btnResend.text = formatString(R.string.action_receive_via_otp)
        } else {
            initStartResendCodeCount(resources.getInteger(R.integer.resend_otp_code_count_30))
            initEnableResendButton(false)
            viewModel.countDownTimer(
                resources.getInteger(R.integer.resend_otp_code_period).toLong(),
                resources.getInteger(R.integer.resend_otp_code_count_30).toLong()
            )
            textViewDidNotReceived.text = formatString(R.string.desc_did_not_receive_code)
            btnResend.text = formatString(R.string.action_resend)
            textViewDidNotReceived.visibility(true)
            tvResendTimer.visibility(true)
            btnResend.visibility(true)
        }
    }

    private fun isTOTPScreen(loginType: String?): Boolean = loginType == LOGIN_TYPE_TOTP

    companion object {
        const val EXTRA_REQUEST = "request"
        const val EXTRA_REQUEST_PAGE = "page"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_COUNTRY_ID = "country_id"
        const val EXTRA_MOBILE_NUMBER = "mobile_number"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_FUND_TRANSFER_REQUEST = "fund_transfer_request"
        const val EXTRA_SELECTED_ACCOUNT = "selected_account"
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_SECURITY_FORM = "security_form"
        const val EXTRA_SERVICE_FEE = "service_fee"
        const val EXTRA_CUSTOM_SERVICE_FEE = "custom_service_fee"

        const val PAGE_LOGIN = "login"
        const val PAGE_MIGRATION = "migration"
        const val PAGE_NOMINATE_PASSWORD = "nominate_password"
        const val PAGE_PASSWORD_RECOVERY = "password_recovery"
        const val PAGE_CHANGE_MOBILE_NUMBER = "change_mobile_number"
        const val PAGE_SECURITY = "security"
        const val PAGE_FUND_TRANSFER_UBP = "fund_transfer_ubp"
        const val PAGE_FUND_TRANSFER_PESONET = "fund_transfer_pesonet"
        const val PAGE_FUND_TRANSFER_INSTAPAY = "fund_transfer_instapay"
        const val PAGE_FUND_TRANSFER_PDDTS = "fund_transfer_pddts"
        const val PAGE_FUND_TRANSFER_SWIFT = "fund_transfer_swift"
        const val PAGE_BILLS_PAYMENT = "bills_payment"

        const val REQ_USER_CONSENT = 100
    }
}
