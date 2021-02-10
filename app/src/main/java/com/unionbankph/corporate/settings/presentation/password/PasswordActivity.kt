package com.unionbankph.corporate.settings.presentation.password

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.data.model.Auth
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import com.unionbankph.corporate.settings.presentation.*
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.concurrent.TimeUnit

class PasswordActivity : BaseActivity<SettingsViewModel>(R.layout.activity_password) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsLoading -> {
                    showProgressAlertDialog(PasswordActivity::class.java.simpleName)
                }
                is ShowSettingsDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowSettingsChangeEmailSuccess -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_UPDATE_EMAIL_ADDRESS)
                    )
                    navigateResultLandingScreen(it.message)
                }
                is ShowSettingsMobileNumberSuccess -> {
                    navigateVerifyAccountScreen(it.auth)
                }
                is ShowSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        validateForm()
    }

    private fun init() {
        if (intent.getStringExtra(EXTRA_PAGE) == PAGE_EDIT_MOBILE_NUMBER) {
            tvPasswordDesc.text = formatString(R.string.msg_enter_password_mobile_number)
        } else {
            tvPasswordDesc.text = formatString(R.string.msg_enter_password_email_address)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        RxView.clicks(btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                when (intent.getStringExtra(EXTRA_PAGE)) {
                    PAGE_EDIT_EMAIL_ADDERSS -> {
                        viewModel.changeEmailAddress(
                            intent.getStringExtra(EXTRA_EMAIL).notNullable(),
                            textInputEditTextPassword.text.toString()
                        )
                    }
                    PAGE_EDIT_MOBILE_NUMBER -> {
                        viewModel.changeMobileNumber(
                            intent.getStringExtra(EXTRA_COUNTRY_CODE_ID).notNullable(),
                            intent.getStringExtra(EXTRA_MOBILE_NUMBER).notNullable(),
                            textInputEditTextPassword.text.toString()
                        )
                    }
                }
            }.addTo(disposables)
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

    private fun navigateVerifyAccountScreen(auth: Auth) {
        val bundle = Bundle()
        bundle.putString(
            OTPActivity.EXTRA_REQUEST,
            JsonHelper.toJson(auth)
        )
        bundle.putString(
            OTPActivity.EXTRA_REQUEST_PAGE,
            OTPActivity.PAGE_CHANGE_MOBILE_NUMBER
        )
        bundle.putString(
            OTPActivity.EXTRA_COUNTRY_ID,
            intent.getStringExtra(EXTRA_COUNTRY_CODE_ID)
        )
        bundle.putString(
            OTPActivity.EXTRA_MOBILE_NUMBER,
            intent.getStringExtra(EXTRA_MOBILE_NUMBER)
        )
        navigator.navigate(
            this,
            OTPActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateResultLandingScreen(message: Message) {
        val bundle = Bundle().apply {
            putString(
                ResultLandingPageActivity.EXTRA_PAGE,
                ResultLandingPageActivity.PAGE_CHANGE_EMAIL
            )
            putString(
                ResultLandingPageActivity.EXTRA_TITLE,
                getString(R.string.title_you_ve_got_mail)
            )
            putString(
                ResultLandingPageActivity.EXTRA_DESC,
                message.message
            )
            putString(
                ResultLandingPageActivity.EXTRA_BUTTON,
                getString(R.string.action_close)
            )
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

    private fun validateForm() {
        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = 8,
            maxLength = 30,
            editText = textInputEditTextPassword
        )

        passwordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        // combine our validation observables into one
        RxCombineValidator(passwordObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                btnSubmit.enableButton(it)
            }.addTo(disposables)
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_COUNTRY_CODE_ID = "country_code_id"
        const val EXTRA_MOBILE_NUMBER = "mobile_number"

        const val PAGE_EDIT_MOBILE_NUMBER = "edit_mobile_number"
        const val PAGE_EDIT_EMAIL_ADDERSS = "edit_email_address"
    }
}
