package com.unionbankph.corporate.settings.presentation.email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.databinding.ActivityUpdateEmailBinding
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.password.PasswordActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class UpdateEmailActivity :
    BaseActivity<ActivityUpdateEmailBinding, SettingsViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        RxView.clicks(binding.btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                val bundle = Bundle()
                bundle.putString(
                    PasswordActivity.EXTRA_PAGE,
                    PasswordActivity.PAGE_EDIT_EMAIL_ADDERSS
                )
                bundle.putString(
                    PasswordActivity.EXTRA_EMAIL,
                    binding.textInputEditTextEmailAddress.text.toString()
                )
                navigator.navigate(
                    this,
                    PasswordActivity::class.java,
                    bundle,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
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

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    private fun validateForm() {
        val emailObservable = RxValidator.createFor(binding.textInputEditTextEmailAddress)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.textInputEditTextEmailAddress.hint
                )
            )
            .email(binding.textInputEditTextEmailAddress.context.getString(R.string.error_invalid_email_address))
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

    override val viewModelClassType: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityUpdateEmailBinding
        get() = ActivityUpdateEmailBinding::inflate
}
