package com.unionbankph.corporate.settings.presentation.update_password

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.form.ActivationPasswordForm
import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordForm
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import com.unionbankph.corporate.databinding.ActivityUpdatePasswordBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class UpdatePasswordActivity :
    BaseActivity<ActivityUpdatePasswordBinding, UpdatePasswordViewModel>() {

    private lateinit var passwordToken: String

    private lateinit var activationId: String

    private lateinit var page: String

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initIntent()
        validateForm()
    }

    private fun initIntent() {
        val intent = intent
        val action = intent.action
        val data = intent.data
        if (action != null && data != null) {
            Timber.d("action: %s", action.toString())
            Timber.d("data: %s", data.toString())
            if (data.toString().contains("passwordToken")) {
                page = PAGE_RESET_PASSWORD
                passwordToken = data.getQueryParameter("passwordToken") ?: ""
            } else if (data.toString().contains("activationId")) {
                binding.tvHeader.text = getString(R.string.title_nominate_password)
                binding.tvPasswordDesc.text = getString(R.string.desc_nominate_password)
                page = PAGE_NOMINATE_PASSWORD
                activationId = data.getQueryParameter("activationId") ?: ""
            }
        } else {
            initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
            page = intent.getStringExtra(EXTRA_REQUEST_PAGE).notNullable()
            setDrawableBackButton(R.drawable.ic_close_white_24dp)
            binding.tilPassword.hint = getString(R.string.hint_new_password)
            binding.tilOldPassword.visibility = View.VISIBLE
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowUPasswordLoading -> {
                    showProgressAlertDialog(UpdatePasswordActivity::class.java.simpleName)
                }
                is ShowUPasswordDismissLoading -> {
                    dismissProgressAlertDialog()
                }

                is ShowUPasswordUpdateSuccess -> {
                    MaterialDialog(this).show {
                        lifecycleOwner(this@UpdatePasswordActivity)
                        title(R.string.title_password_changed)
                        cancelOnTouchOutside(false)
                        message(R.string.msg_password_changed)
                        positiveButton(
                            res = R.string.action_close,
                            click = {
                                it.dismiss()
                                when (page) {
                                    PAGE_UPDATE_PASSWORD -> {
                                        this@UpdatePasswordActivity.onBackPressed()
                                    }
                                    PAGE_RESET_PASSWORD -> {
                                        navigator.navigateClearUpStack(
                                            this@UpdatePasswordActivity,
                                            LoginActivity::class.java,
                                            Bundle().apply {
                                                putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                                            },
                                            isClear = true,
                                            isAnimated = true
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                is ShowUPasswordSuccess -> {
                    when (page) {
                        PAGE_NOMINATE_PASSWORD -> {
                            val bundle = Bundle()
                            bundle.putString(
                                OTPActivity.EXTRA_REQUEST,
                                JsonHelper.toJson(it.auth)
                            )
                            bundle.putString(
                                OTPActivity.EXTRA_REQUEST_PAGE,
                                OTPActivity.PAGE_NOMINATE_PASSWORD
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
                    }
                }
                is ShowUPasswordNominatePasswordError -> {
                    initNominatePasswordError(JsonHelper.toJson(it.apiError))
                }
                is ShowUPasswordError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        RxView.clicks(binding.btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                when (page) {
                    PAGE_UPDATE_PASSWORD -> viewModel.changePassword(
                        ChangePasswordForm(
                            oldPassword = binding.etOldPassword.text.toString(),
                            newPassword = binding.etPassword.text.toString().trim(),
                            confirmNewPassword = binding.vetCPassword.text.toString().trim()
                        )
                    )
                    PAGE_RESET_PASSWORD -> viewModel.resetPasswordNew(
                        ResetPasswordForm(
                            passwordToken = passwordToken,
                            password = binding.etPassword.text.toString().trim(),
                            passwordConfirm = binding.etCPassword.text.toString().trim()
                        )
                    )
                    PAGE_NOMINATE_PASSWORD -> viewModel.nominatePassword(
                        activationPasswordForm = ActivationPasswordForm(
                            activationId = activationId
                        ),
                        password = binding.etPassword.text.toString().trim()
                    )
                }
            }.addTo(disposables)
    }

    override fun onBackPressed() {
        Timber.d("onBackPressed2")
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
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

    private fun validateForm() {
        val oldPasswordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = 8,
            maxLength = 30,
            editText = binding.etOldPassword
        )
        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = 8,
            maxLength = 30,
            editText = binding.etPassword
        )

        val cPasswordObservable = RxValidator.createFor(binding.etCPassword)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.tilCPassword.hint
                )
            )
            .sameAs(binding.etPassword, getString(R.string.error_compare_password))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordLimitObservable = RxValidator.createFor(binding.etPassword)
            .minLength(8)
            .maxLength(30)
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordAlphaObservable = RxValidator.createFor(binding.etPassword)
            .patternMatches(
                getString(R.string.error_no_aplha),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_ALPHA)
            )
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordNumberObservable = RxValidator.createFor(binding.etPassword)
            .patternMatches(
                getString(R.string.error_no_number),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_NUMBER)
            )
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordSymbolObservable = RxValidator.createFor(binding.etPassword)
            .patternMatches(
                getString(R.string.error_no_symbol),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_SYMBOL)
            )
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        oldPasswordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        passwordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        cPasswordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        passwordLimitObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPasswrd1) }
            .addTo(disposables)

        passwordAlphaObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPasswrd2) }
            .addTo(disposables)

        passwordNumberObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPasswrd3) }
            .addTo(disposables)

        passwordSymbolObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPasswrd4) }
            .addTo(disposables)

        (if (page == PAGE_UPDATE_PASSWORD)
            RxCombineValidator(
                oldPasswordObservable,
                passwordObservable,
                cPasswordObservable,
                passwordLimitObservable,
                passwordAlphaObservable,
                passwordNumberObservable,
                passwordSymbolObservable
            )
        else
            RxCombineValidator(
                passwordObservable,
                cPasswordObservable,
                passwordLimitObservable,
                passwordAlphaObservable,
                passwordNumberObservable,
                passwordSymbolObservable
            ))
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.btnSubmit.enableButton(it)
            }.addTo(disposables)
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

    companion object {
        const val EXTRA_REQUEST_PAGE = "page"

        const val PAGE_RESET_PASSWORD = "reset_password"
        const val PAGE_NOMINATE_PASSWORD = "nominate_password"
        const val PAGE_UPDATE_PASSWORD = "update_password"
    }

    override val layoutId: Int
        get() = R.layout.activity_update_password

    override val viewModelClassType: Class<UpdatePasswordViewModel>
        get() = UpdatePasswordViewModel::class.java
}
