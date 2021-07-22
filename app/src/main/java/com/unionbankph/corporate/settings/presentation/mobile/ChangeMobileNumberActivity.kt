package com.unionbankph.corporate.settings.presentation.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.clear
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.showErrorBottomSheet
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityChangeMobileNumberBinding
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsGetCorporateUser
import com.unionbankph.corporate.settings.presentation.country.CountryActivity
import com.unionbankph.corporate.settings.presentation.password.PasswordActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ChangeMobileNumberActivity :
    BaseActivity<ActivityChangeMobileNumberBinding, SettingsViewModel>() {

    private var countryCode: CountryCode? = null

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsGetCorporateUser -> {
                    countryCode = it.corporateUser.countryCode
                    initCountryDefault(countryCode)
                    binding.editTextMobileNumber.setText(it.corporateUser.mobileNumber)
                }
                is ShowSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        viewModel.getCorporateUser()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        RxView.clicks(binding.btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                if (viewModel.isValidFormInput.value.notNullable()) {
                    navigatePassword()
                } else {
                    showErrorBottomSheet(
                        title = formatString(R.string.error_invalid_mobile_number),
                        message = formatString(R.string.error_input_valid_mobile_number),
                        actionPositive = formatString(R.string.action_okay)
                    )
                }
            }.addTo(disposables)

        binding.countryCodePicker.root.setOnClickListener {
            navigateCountryScreen()
        }
    }

    private fun navigatePassword() {
        val bundle = Bundle()
        bundle.putString(
            PasswordActivity.EXTRA_PAGE,
            PasswordActivity.PAGE_EDIT_MOBILE_NUMBER
        )
        bundle.putString(
            PasswordActivity.EXTRA_COUNTRY_CODE_ID,
            countryCode?.id.toString()
        )
        bundle.putString(
            PasswordActivity.EXTRA_MOBILE_NUMBER,
            binding.editTextMobileNumber.text.toString()
        )
        navigator.navigate(
            this,
            PasswordActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_COUNTRY) {
                countryCode = JsonHelper.fromJson(it.payload)
                initCountryDefault(countryCode)
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

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    private fun initCountryDefault(selectedCountryCode: CountryCode?) {
        validateForm(Constant.getDefaultCountryCode().code == selectedCountryCode?.code)
        binding.editTextMobileNumber.clear()
        viewUtil.setEditTextMaxLength(
            binding.editTextMobileNumber,
            resources.getInteger(
                if (Constant.getDefaultCountryCode().code == countryCode?.code) {
                    R.integer.max_length_mobile_number_ph
                } else {
                    R.integer.max_length_mobile_number
                }
            )
        )
        binding.countryCodePicker.textViewCallingCode.text = selectedCountryCode?.callingCode
        binding.countryCodePicker.imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${selectedCountryCode?.code?.toLowerCase()}")
        )
    }

    private fun validateForm(isCountryPH: Boolean) {
        formDisposable.clear()
        val mobileNumberObservable =
            if (isCountryPH) {
                RxValidator.createFor(binding.editTextMobileNumber)
                    .nonEmpty(
                        String.format(
                            getString(R.string.error_specific_field),
                            getString(R.string.hint_mobile_number)
                        )
                    )
                    .patternMatches(
                        getString(R.string.error_input_valid_mobile_number),
                        Pattern.compile(ViewUtil.REGEX_FORMAT_MOBILE_NUMBER_PH)
                    )
                    .minLength(
                        resources.getInteger(R.integer.max_length_mobile_number_ph),
                        String.format(
                            getString(R.string.error_validation_min),
                            resources.getInteger(R.integer.max_length_mobile_number_ph).toString()
                        )
                    )
                    .onFocusChanged()
                    .onValueChanged()
                    .toObservable()
                    .debounce {
                        Observable.timer(
                            resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                            TimeUnit.MILLISECONDS
                        )
                    }
            } else {
                viewUtil.rxTextChanges(
                    isFocusChanged = true,
                    isValueChanged = true,
                    minLength = resources.getInteger(R.integer.min_length_field),
                    maxLength = resources.getInteger(R.integer.max_length_field_100),
                    editText = binding.editTextMobileNumber,
                    customErrorMessage = String.format(
                        getString(R.string.error_specific_field),
                        getString(R.string.hint_mobile_number)
                    )
                )
            }
        RxCombineValidator(
            mobileNumberObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(formDisposable)
    }

    private fun navigateCountryScreen() {
        val bundle = Bundle()
        bundle.putSerializable(
            CountryActivity.EXTRA_PAGE,
            CountryActivity.CountryScreen.SETTINGS_SCREEN
        )
        navigator.navigate(
            this,
            CountryActivity::class.java,
            bundle,
            false,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT

        )
    }

    override val viewModelClassType: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityChangeMobileNumberBinding
        get() = ActivityChangeMobileNumberBinding::inflate
}
