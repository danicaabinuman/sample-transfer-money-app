package com.unionbankph.corporate.auth.presentation.password_recovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ProgressBarDialog
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.auth.data.form.PasswordRecoveryForm
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityPasswordRecoveryBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class PasswordRecoveryActivity :
    BaseActivity<ActivityPasswordRecoveryBinding, PasswordRecoveryViewModel>() {

    private var dialog: ProgressBarDialog? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm(binding.etEmail)
        binding.etEmail.post {
            if (intent.getStringExtra(EXTRA_EMAIL).notNullable().isNotEmpty()) {
                binding.etEmail.setText(intent.getStringExtra(EXTRA_EMAIL))
            }
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowPRecoveryLoading -> {
                    dialog = ProgressBarDialog.newInstance()
                    dialog?.show(
                        supportFragmentManager,
                        PasswordRecoveryActivity::class.java.simpleName
                    )
                }
                is ShowPRecoveryDismissLoading -> {
                    dialog?.dismiss()
                }
                is ShowPRecoverySuccess -> {
                    val bundle = Bundle()
                    bundle.putString(
                        OTPActivity.EXTRA_REQUEST,
                        JsonHelper.toJson(it.auth)
                    )
                    bundle.putString(
                        OTPActivity.EXTRA_REQUEST_PAGE,
                        OTPActivity.PAGE_PASSWORD_RECOVERY
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
                is ShowPRecoveryError -> {
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
                viewModel.resetPass(
                    PasswordRecoveryForm(
                        binding.etEmail.text.toString().trim()
                    )
                )
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

    private fun validateForm(etUsername: EditText) {
        val emailObservable = RxValidator.createFor(etUsername)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.tilEmail.hint
                )
            )
            .email(getString(R.string.error_invalid_email_address))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        emailObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        RxCombineValidator(emailObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.btnSubmit.enableButton(it)
            }.addTo(disposables)
    }

    companion object {
        const val EXTRA_EMAIL = "email"
    }

    override val viewModelClassType: Class<PasswordRecoveryViewModel>
        get() = PasswordRecoveryViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityPasswordRecoveryBinding
        get() = ActivityPasswordRecoveryBinding::inflate
}
