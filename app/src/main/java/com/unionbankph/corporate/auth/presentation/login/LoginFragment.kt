package com.unionbankph.corporate.auth.presentation.login

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.biometric.BiometricManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener
import com.jakewharton.rxbinding2.view.RxView
import com.mtramin.rxfingerprint.RxFingerprint
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.auth.data.form.LoginFingerprintForm
import com.unionbankph.corporate.auth.data.form.LoginForm
import com.unionbankph.corporate.auth.presentation.migration.migration_selection.MigrationSelectionActivity
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.auth.presentation.password_recovery.PasswordRecoveryActivity
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.URLDataEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.selection.DaoSelectionActivity
import com.unionbankph.corporate.settings.presentation.fingerprint.FaceIDBottomSheet
import com.unionbankph.corporate.settings.presentation.fingerprint.FingerprintBottomSheet
import com.unionbankph.corporate.settings.presentation.learn_more.LearnMoreActivity
import com.unionbankph.corporate.settings.presentation.totp.TOTPActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_login.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by herald on 2/17/21
 */
class LoginFragment : BaseFragment<LoginViewModel>(R.layout.fragment_login),
    FingerprintBottomSheet.OnFingerPrintListener,
    InstallStateUpdatedListener,
    OnSuccessListener<AppUpdateInfo>,
    ImeOptionEditText.OnImeOptionListener,
    FaceIDBottomSheet.OnFaceIdListener{

    private lateinit var imeOptionEditText: ImeOptionEditText

    private var appUpdateManager: AppUpdateManager? = null

    private var fingerprintBottomSheet: FingerprintBottomSheet? = null

    private var faceIDBottomSheet: FaceIDBottomSheet? = null

    private var isShownInitialFingerprint: Boolean = false

    private var isShownInUpdateApp: Boolean = false

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        setMargins(textViewVersion, 0, getStatusBarHeight(), 0, 0)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        if (getAppCompatActivity().intent.getBooleanExtra(EXTRA_SPLASH_SCREEN, true)) {
            viewModel.getEnabledFeatures()
        } else {
            init()
        }
    }

    private fun initBinding() {
        viewModel.isTrustedDeviceOutput
            .subscribe {
                buttonGenerateOTP.visibility(it)
            }.addTo(disposables)
        viewModel.token
            .subscribe {
                if(RxFingerprint.isAvailable(getAppCompatActivity())) { showFingerprintImageView(it != "")
                }else if(BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                        .BIOMETRIC_SUCCESS) { showFaceIDImageView(it !="") }
            }.addTo(disposables)
        viewModel.fullName
            .subscribe {
                textViewFullname.text = it
            }.addTo(disposables)
        viewModel.emailAddress
            .subscribe {
                getEditTextUsername().setText(it)
                val fingerPrintToken = viewModel.token.value.notNullable()
                if (getAppCompatActivity().intent.getBooleanExtra(EXTRA_SPLASH_SCREEN, true)) {
                    initAnimationLogo()
                    runPostDelayed(
                        {
                            if (it != "") {
                                fingerprintViews(fingerPrintToken)
                            } else {
                                initFreshLogin()
                            }
                        }, 100
                    )
                } else {
                    imageViewLogo.visibility(true)
                    imageViewLogoAnimate.visibility(false)
                    viewUtil.startAnimateView(
                        true,
                        constraintLayoutContent,
                        android.R.anim.fade_in
                    )
                    if (it != "") {
                        fingerprintViews(fingerPrintToken)
                    } else {
                        initFreshLogin()
                    }
                }
            }.addTo(disposables)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onPause() {
        super.onPause()
        fingerprintBottomSheet?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager?.unregisterListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Timber.d("Update flow success! Result code: $resultCode")
                }
                // If the update is cancelled or fails,
                // you can request to start the update again.
                Activity.RESULT_CANCELED -> {
                    Timber.d("Update flow result canceled! Result code: $resultCode")
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Timber.d("Update flow failed! Result code: $resultCode")
                }
            }
        }
    }

    override fun onSuccess(appUpdateInfo: AppUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        ) {
            try {
                isShownInUpdateApp = true
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    getAppCompatActivity(),
                    REQUEST_CODE_UPDATE
                )
            } catch (e: IntentSender.SendIntentException) {
                Timber.e(e, "checkUpdates")
            }
        } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            showFlexibleUpdateNotification()
        } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            // If an in-app update is already running, resume the update.
            try {
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    getAppCompatActivity(),
                    REQUEST_CODE_UPDATE
                )
            } catch (e: IntentSender.SendIntentException) {
                Timber.e(e, "onSuccess")
            }
        }
    }

    override fun onStateUpdate(state: InstallState) {
        // Show module progress, log state, or install the update.
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showFlexibleUpdateNotification()
        } else if (state.installStatus() == InstallStatus.INSTALLED) {
            viewUtil.snackBarMessage(
                constraintLayoutLogin,
                getString(R.string.msg_playstore_update_success),
                Snackbar.LENGTH_SHORT
            ).show()
            if (appUpdateManager != null) {
                appUpdateManager?.unregisterListener(this)
            }
        } else if (state.installStatus() == InstallStatus.FAILED) {
            viewUtil.snackBarMessage(
                constraintLayoutLogin,
                getString(R.string.msg_playstore_update_failed),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    override fun onCompleteFaceID(token: String) {
        viewModel.userLoginFingerPrint(
            LoginFingerprintForm(fingerprintToken = token, udid = settingsUtil.getUdId())
        )
    }

    override fun onErrorFaceID() {
        showFaceIDImageView(false)
        viewModel.token.onNext("")
    }

    override fun onDismissFaceIDialog() {
        if ((BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                .BIOMETRIC_SUCCESS) &&
            sharedPreferenceUtil.fingerPrintTokenSharedPref().get() != ""
        ) {
            showFaceIDImageView(true)
        }
    }

    override fun onCompleteFingerprint(token: String) {
        viewModel.userLoginFingerPrint(
            LoginFingerprintForm(fingerprintToken = token, udid = settingsUtil.getUdId())
        )
    }

    override fun onErrorFingerprint() {
        showFingerprintImageView(false)
        viewModel.token.onNext("")
    }

    override fun onDismissFingerprintDialog() {
        if (RxFingerprint.isAvailable(getAppCompatActivity()) &&
            sharedPreferenceUtil.fingerPrintTokenSharedPref().get() != ""
        ) {
            showFingerprintImageView(true)
        }
    }

    override fun onDoneKeyAction(v: TextView?, actionId: Int, event: KeyEvent?) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            when (v?.id) {
                R.id.etPassword, R.id.etPasswordSME -> {
                    if (isValidForm) {
                        submitForm()
                    }
                    viewUtil.dismissKeyboard(getAppCompatActivity())
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[LoginViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowLoginLoading -> {
                    showProgressAlertDialog(LoginActivity::class.java.simpleName)
                }
                is ShowLoginDismissLoading -> dismissProgressAlertDialog()

                is ShowLoginSuccess -> {
                    initLoginSuccess(it)
                }
                is ShowLoginSkipOTPSuccess,
                is ShowLoginFingerprintSuccess -> {
                    navigateDashboardScreen()
                }
                is ShowLoginNoPrivacyPolicy -> {
                    navigatePrivacyPolicyScreen()
                }
                is ShowLoginUserGetEmailAddress -> {
                    getEditTextUsername().setText(it.email)
                }
                // Errors
                is ShowLoginConnectivityError -> {
                    showErrorAndExit(message = it.throwable.message.notEmpty())
                }
                is ShowLoginError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.startLogin.observe(this, EventObserver {
            if (it) {
                init()
            }
        })
    }

    private fun showErrorAndExit(
        title: String = formatString(R.string.title_error),
        message: String
    ) {
        MaterialDialog(getAppCompatActivity()).show {
            lifecycleOwner(getAppCompatActivity())
            title(text = title)
            cancelOnTouchOutside(false)
            message(text = message)
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                    getAppCompatActivity().finish()
                }
            )
        }
    }

    private fun init() {
        initBinding()
        validateForm(getEditTextUsername(), getEditTextPassword())
        textViewVersion.text = getString(R.string.app_version_number)
        initImeOptionEditText()
        checkUpdates()
        clearCache()
        initListener()
        viewModel.hasFingerPrint()
        viewModel.hasTOTP()
        viewModel.refreshNotificationTokenIfNull()
        if (isSME) {
            tv_sign_up.text = formatString(R.string.action_sign_up).toHtmlSpan()
        } else {
            tv_sign_up.text = formatString(R.string.title_forgot_password).toUpperCase()
            tv_sign_up.setTypeface(tv_sign_up.typeface, Typeface.BOLD)
        }
    }

    private fun initListener() {
        initClickButtonLogin()
        initClickImgFingerPrint()
        initClickImgFaceID()
        initClickTextViewFullname()
        initClickTextViewLearnMore()
        initClickButtonGenerateTOTP()
        textViewMigration.makeLinks(
            Pair(getString(R.string.action_here), View.OnClickListener {
                navigateMigrationScreen()
            })
        )
        textViewMigration.setOnClickListener {
            navigateMigrationScreen()
        }
        iv_show_password.setOnClickListener {
            iv_show_password.isActivated = !iv_show_password.isActivated
            if (iv_show_password.isActivated) {
                etPasswordSME.transformationMethod = null
            } else {
                etPasswordSME.transformationMethod = PasswordTransformationMethod()
            }
            etPasswordSME.setSelection(etPasswordSME.length())
        }
        setOnClickListenerSignUp()
        btn_open_business_account.setOnClickListener {
            navigateDaoSelectionScreen()
        }
        btn_apply_loan.setOnClickListener {
            showDisclaimerDialog()
        }
        btn_initial_login.setOnClickListener {
            loginViews()
        }
        tvForgotPassword.setOnClickListener {
            navigatePasswordRecoveryScreen()
        }
    }

    private fun setOnClickListenerSignUp() {
        tv_sign_up.setOnClickListener {
            if (isSME) {
                navigateDaoSelectionScreen()
            } else {
                navigatePasswordRecoveryScreen()
            }
        }
    }

    private fun initImeOptionEditText() {
        imeOptionEditText = ImeOptionEditText()
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.addEditText(getEditTextUsername(), getEditTextPassword())
        imeOptionEditText.startListener()
    }

    private fun clearCache() {
        App.stopSessionJobService(getAppCompatActivity())
        sharedPreferenceUtil.isLoggedIn().set(false)
        cacheManager.clear(CacheManager.ACCESS_TOKEN)
        cacheManager.clear(CacheManager.CORPORATE_USER)
        cacheManager.clear(CacheManager.ROLE)
    }

    private fun validateForm(etUsername: EditText, etPassword: EditText) {
        val emailObservable = RxValidator.createFor(etUsername)
            .nonEmpty(etUsername.context.getString(R.string.error_this_field))
            .email(etUsername.context.getString(R.string.error_invalid_email_address))
            .onValueChanged()
            .toObservable()
            .debounce { Observable.timer(100, TimeUnit.MILLISECONDS) }

        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = 1,
            maxLength = 100,
            editText = etPassword
        )

        RxCombineValidator(emailObservable, passwordObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                isValidForm = it
                buttonLogin.loginEnableButton(it)
            }.addTo(disposables)
    }

    private fun initFreshLogin() {
        if (isSME) {
            if (!viewModel.cdaoFeature.value.notNullable()) {
                loginViews()
            } else {
                btn_open_business_account.visibility(true)
                btn_apply_loan.visibility(true)
                btn_initial_login.visibility(true)
                buttonLogin.visibility(false)
                tilUsername.visibility(false)
                tilPassword.visibility(false)
                cardViewEmail.visibility(false)
                cardViewPassword.visibility(false)
                tv_sign_up.visibility(true)
                tv_sign_up.text =
                    formatString(R.string.action_sign_up_with_existing)
                        .toUpperCase()
                        .toHtmlSpan()
                tv_sign_up.setOnClickListener {
                    navigator.navigateBrowser(
                        getAppCompatActivity(),
                        URLDataEnum.ENROLLMENT_LINK
                    )
                }
                val constraintSet = ConstraintSet()
                constraintSet.connect(
                    tv_sign_up.id,
                    ConstraintSet.TOP,
                    btn_initial_login.id,
                    ConstraintSet.BOTTOM
                )
                constraintSet.clone(constraint_layout)
            }
        } else {
            tv_sign_up.visibility(true)
            tilUsername.visibility(true)
            tilPassword.visibility(true)
            buttonLogin.visibility(true)
            cardViewEmail.visibility(false)
            cardViewPassword.visibility(false)
        }
        buttonLogin.loginEnableButton(false)
        textViewMigration.visibility(true)
        textViewWelcomeBack.visibility(false)
        textViewFullname.visibility(false)
        buttonGenerateOTP.visibility(false)
    }

    private fun fingerprintViews(token: String) {
        val tilUsernameParams = tilUsername.layoutParams as ViewGroup.MarginLayoutParams
        val buttonLoginParams = buttonLogin.layoutParams as ViewGroup.MarginLayoutParams
        tilUsernameParams.setMargins(
            tilUsernameParams.leftMargin,
            resources.getDimensionPixelSize(R.dimen.content_group_spacing),
            tilUsernameParams.rightMargin,
            tilUsernameParams.bottomMargin
        )
        buttonLoginParams.setMargins(
            buttonLoginParams.leftMargin,
            resources.getDimensionPixelSize(R.dimen.content_spacing_xl),
            buttonLoginParams.rightMargin,
            buttonLoginParams.bottomMargin
        )
        tvForgotPassword.visibility(false)
        textViewWelcomeBack.visibility(true)
        textViewFullname.visibility(true)
        buttonLogin.visibility(true)
        cardViewEmail.visibility(false)
        cardViewPassword.visibility(false)
        tilUsername.visibility(false)
        tilPassword.visibility(false)
        tv_sign_up.visibility(false)
        textViewMigration.visibility(false)
        buttonLogin.text = getString(R.string.use_password)
        buttonLogin.loginEnableButton(true)
        if (token != "") {
            if (getAppCompatActivity().intent.getBooleanExtra(EXTRA_SPLASH_SCREEN, true)
                && !isShownInitialFingerprint
            ) {
                runPostDelayed(
                    {
                        isShownInitialFingerprint = true
                        showFingerPrint(token)
                    }, 1500
                )
            } else {
                showFingerPrint(token)
            }
        }
    }

    private fun loginViews() {
        val tilUsernameParams = tilUsername.layoutParams as ViewGroup.MarginLayoutParams
        val tilPasswordParams = tilPassword.layoutParams as ViewGroup.MarginLayoutParams
        val buttonLoginParams = buttonLogin.layoutParams as ViewGroup.MarginLayoutParams
        tilUsernameParams.setMargins(
            tilUsernameParams.leftMargin,
            resources.getDimensionPixelSize(R.dimen.content_group_spacing),
            tilUsernameParams.rightMargin, tilUsernameParams.bottomMargin
        )
        tilPasswordParams.setMargins(
            tilPasswordParams.leftMargin,
            resources.getDimensionPixelSize(R.dimen.content_spacing_small),
            tilPasswordParams.rightMargin,
            tilPasswordParams.bottomMargin
        )
        buttonLoginParams.setMargins(
            buttonLoginParams.leftMargin,
            resources.getDimensionPixelSize(
                if (App.isSME()) R.dimen.content_spacing_small else R.dimen.content_spacing
            ),
            buttonLoginParams.rightMargin, buttonLoginParams.bottomMargin
        )
        val constraintSet = ConstraintSet()
        constraintSet.connect(
            tv_sign_up.id, ConstraintSet.TOP,
            buttonLogin.id, ConstraintSet.BOTTOM
        )
        constraintSet.clone(constraint_layout)
        if (isSME) {
            tilUsername.setVisible(false)
            tilPassword.setVisible(false)
            cardViewEmail.visibility(true)
            cardViewPassword.visibility(true)
            tvForgotPassword.visibility(true)
            btn_initial_login.visibility(false)
            btn_open_business_account.visibility(false)
            btn_apply_loan.visibility(false)
            tv_sign_up.text = formatString(R.string.action_sign_up).toHtmlSpan()
            tv_sign_up.visibility(viewModel.cdaoFeature.value.notNullable())
            setOnClickListenerSignUp()
        } else {
            tilUsername.visibility(true)
            tilPassword.visibility(true)
            cardViewEmail.visibility(false)
            cardViewPassword.visibility(false)
            tv_sign_up.visibility(true)
        }
        textViewWelcomeBack.visibility(false)
        textViewFullname.visibility(false)
        textViewMigration.visibility(true)
        buttonLogin.visibility(true)
        buttonLogin.text = formatString(R.string.action_login)
        buttonLogin.loginEnableButton(false)
        if (getEditTextUsername().length() > 0) {
            getEditTextPassword().requestFocus()
            viewUtil.showKeyboard(getAppCompatActivity())
        }
    }

    private fun usePasswordViews() {
        validateForm(getEditTextUsername(), getEditTextPassword())
        val tilUsernameParams = tilUsername.layoutParams as ViewGroup.MarginLayoutParams
        val tilPasswordParams = tilPassword.layoutParams as ViewGroup.MarginLayoutParams
        val buttonLoginParams = buttonLogin.layoutParams as ViewGroup.MarginLayoutParams
        tilUsernameParams.setMargins(
            tilUsernameParams.leftMargin,
            resources.getDimensionPixelSize(R.dimen.content_group_spacing),
            tilUsernameParams.rightMargin,
            tilUsernameParams.bottomMargin
        )
        tilPasswordParams.setMargins(
            tilPasswordParams.leftMargin,
            resources.getDimensionPixelSize(R.dimen.content_group_spacing),
            tilPasswordParams.rightMargin,
            tilPasswordParams.bottomMargin
        )
        buttonLoginParams.setMargins(
            buttonLoginParams.leftMargin,
            resources.getDimensionPixelSize(
                if (isSME) {
                    R.dimen.content_spacing_half
                } else {
                    R.dimen.content_spacing
                }
            ),
            buttonLoginParams.rightMargin,
            buttonLoginParams.bottomMargin
        )
        val constraintSet = ConstraintSet()
        constraintSet.connect(
            tv_sign_up.id, ConstraintSet.TOP,
            buttonLogin.id, ConstraintSet.BOTTOM
        )
        constraintSet.clone(constraint_layout)
        if (isSME) {
            cardViewPassword.visibility(true)
            tilPassword.setVisible(false)
            tvForgotPassword.setVisible(true)
            tv_sign_up.text = formatString(R.string.action_sign_up).toHtmlSpan()
            tv_sign_up.visibility(viewModel.cdaoFeature.value.notNullable())
            setOnClickListenerSignUp()
        } else {
            tilPassword.visibility(true)
            tv_sign_up.visibility(true)
        }
        textViewMigration.visibility = View.GONE
        buttonLogin.text = formatString(R.string.action_login)
        buttonLogin.loginEnableButton(false)
        getEditTextPassword().requestFocus()
        viewUtil.showKeyboard(getAppCompatActivity())
    }

    private fun showFingerPrint(token: String) {
        if (RxFingerprint.isHardwareDetected(getAppCompatActivity())
            && !isShownInUpdateApp
            && App.isActivityVisible()
        ) {
            fingerprintBottomSheet = FingerprintBottomSheet.newInstance(
                token,
                FingerprintBottomSheet.DECRYPT_TYPE
            )
            fingerprintBottomSheet?.setOnFingerPrintListener(this)
            fingerprintBottomSheet?.show(
                childFragmentManager,
                LoginActivity::class.java.simpleName
            )
        }else if(BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                .BIOMETRIC_SUCCESS
            && !isShownInUpdateApp
            && App.isActivityVisible()){
            faceIDBottomSheet = FaceIDBottomSheet.newInstance(
                token,
                FaceIDBottomSheet.DECRYPT_TYPE
            )
            faceIDBottomSheet?.setOnFaceIdListener(this)
            faceIDBottomSheet?.show(
                childFragmentManager,
                LoginActivity::class.java.simpleName
            )
        }
    }

    private fun navigateDashboardScreen() {
        val bundle = Bundle().apply {
            putString(
                AutobahnFirebaseMessagingService.EXTRA_DATA,
                getAppCompatActivity().intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
            )
        }
        navigator.navigateClearStacks(
            getAppCompatActivity(),
            DashboardActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigatePrivacyPolicyScreen() {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        navigator.navigate(
            getAppCompatActivity(),
            PrivacyPolicyActivity::class.java,
            Bundle().apply {
                putString(
                    PrivacyPolicyActivity.EXTRA_REQUEST_PAGE,
                    PrivacyPolicyActivity.PAGE_LOGIN
                )
                putString(
                    AutobahnFirebaseMessagingService.EXTRA_DATA,
                    getAppCompatActivity().intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
                )
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateDaoSelectionScreen() {
        navigator.navigate(
            getAppCompatActivity(),
            DaoSelectionActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateDaoScreen() {
        navigator.navigate(
            getAppCompatActivity(),
            DaoActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateMigrationScreen() {
        navigator.navigate(
            getAppCompatActivity(),
            MigrationSelectionActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initClickButtonLogin() {
        RxView.clicks(buttonLogin)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                if (buttonLogin.text == getString(R.string.action_login)) {
                    submitForm()
                } else if (buttonLogin.text == getString(R.string.use_password)) {
                    usePasswordViews()
                }
            }.addTo(disposables)
    }

    private fun submitForm() {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        constraintLayoutLogin.requestFocus()
        constraintLayoutLogin.isFocusableInTouchMode = true
        viewModel.login(
            LoginForm(
                username = getEditTextUsername().text.toString().trim(),
                password = getEditTextPassword().text.toString().trim()
            )
        )
    }

    private fun navigatePasswordRecoveryScreen() {
        val bundle = bundleOf(
            PasswordRecoveryActivity.EXTRA_EMAIL to getEditTextUsername().text.toString()
        )
        navigator.navigate(
            getAppCompatActivity(),
            PasswordRecoveryActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initClickButtonGenerateTOTP() {
        RxView.clicks(buttonGenerateOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigator.navigate(
                    getAppCompatActivity(),
                    TOTPActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)
    }

    private fun initClickTextViewLearnMore() {
        RxView.clicks(textViewLearnMore)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigator.navigate(
                    getAppCompatActivity(),
                    LearnMoreActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)
    }

    private fun initClickTextViewFullname() {
        RxView.clicks(textViewFullname)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                loginViews()
            }.addTo(disposables)
    }

    private fun initClickImgFingerPrint() {
        RxView.clicks(imgFingerPrint)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewUtil.dismissKeyboard(getAppCompatActivity())
                fingerprintViews(viewModel.token.value.notNullable())
            }.addTo(disposables)
    }

    private fun initClickImgFaceID() {
        RxView.clicks(imgFaceID)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewUtil.dismissKeyboard(getAppCompatActivity())
                fingerprintViews(viewModel.token.value.notNullable())
            }.addTo(disposables)
    }

    private fun initLoginSuccess(it: ShowLoginSuccess) {
        getEditTextUsername().clearFocus()
        getEditTextPassword().clearFocus()
        val bundle = Bundle()
        bundle.putString(
            OTPActivity.EXTRA_REQUEST,
            JsonHelper.toJson(it.auth)
        )
        bundle.putString(
            OTPActivity.EXTRA_EMAIL, getEditTextUsername().text.toString().trim()
        )
        bundle.putString(
            OTPActivity.EXTRA_REQUEST_PAGE, OTPActivity.PAGE_LOGIN
        )
        bundle.putString(
            AutobahnFirebaseMessagingService.EXTRA_DATA,
            getAppCompatActivity().intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        navigator.navigate(
            getAppCompatActivity(),
            OTPActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showDisclaimerDialog() {
        val bottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_disclaimer),
            formatString(R.string.msg_apply_loan_disclaimer),
            formatString(R.string.action_proceed),
            formatString(R.string.action_cancel)
        )
        bottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                navigator.navigateBrowser(
                    getAppCompatActivity(),
                    URLDataEnum.SEEK_CAP_LINK
                )
                bottomSheet.dismiss()
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                bottomSheet.dismiss()
            }
        })
        bottomSheet.show(
            childFragmentManager,
            DaoActivity.TAG_GO_BACK_DAO_DIALOG
        )
    }

    private fun checkUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(getAppCompatActivity())
        appUpdateManager?.registerListener(this)
    }

    private fun showFingerprintImageView(isShow: Boolean) {
        Observable.just(isShow)
            .delay(
                resources.getInteger(R.integer.time_delay_fingerprint).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                imgFingerPrint.visibility(it)
            }.addTo(disposables)
    }

    private fun showFaceIDImageView(isShow: Boolean) {
        Observable.just(isShow)
            .delay(
                resources.getInteger(R.integer.time_delay_fingerprint).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                imgFaceID.visibility(it)
            }.addTo(disposables)
    }

    private fun showFlexibleUpdateNotification() {
        viewUtil.snackBarWithAction(
            constraintLayoutLogin,
            getString(R.string.msg_playstore_update),
            getString(R.string.action_install).toUpperCase(),
            Snackbar.LENGTH_INDEFINITE,
            View.OnClickListener {
                appUpdateManager?.completeUpdate()
            }
        ).show()
    }

    private fun initAnimationLogo() {
        imageViewLogo.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    imageViewLogo.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    runPostDelayed(
                        {
                            animateContent()
                        }, 1000
                    )
                }
            }
        )
    }

    private fun animateContent() {
        runPostDelayed(
            {
                viewUtil.startAnimateView(true, constraintLayoutContent, android.R.anim.fade_in)
            }, 250
        )
        val location = IntArray(2)
        imageViewLogo.getLocationOnScreen(location)
        val y = location[1]
        val objectAnimator =
            ObjectAnimator.ofFloat(imageViewLogoAnimate, "y", y.toFloat())
        Timber.d("y axis:${y.toFloat()}")
        objectAnimator.duration = resources.getInteger(R.integer.anim_duration_medium).toLong()
        objectAnimator.start()
        runPostDelayed(
            {
                imageViewLogo.visibility(true)
                runPostDelayed(
                    {
                        imageViewLogoAnimate.visibility(false)
                    }, 50
                )
            }, 550
        )
    }

    private fun getEditTextUsername() = if (App.isSME()) etUsernameSME else etUsername

    private fun getEditTextPassword() = if (App.isSME()) etPasswordSME else etPassword

    companion object {
        const val EXTRA_SPLASH_SCREEN = "splash_screen"
        const val REQUEST_CODE_UPDATE = 1
    }

}