package com.unionbankph.corporate.user_creation.presentation.enter_contact_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.auth.presentation.otp.*
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcEnterContactInfoBinding
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class UcEnterContactInfoFragment :
    BaseFragment<FragmentUcEnterContactInfoBinding, UcEnterContactInfoViewModel>() {

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        handleOnBackPressed()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
        initViewBinding()
        initOnClicks()
    }

    private fun initOnClicks() {
        binding.buttonNext.setOnClickListener { attemptSubmit() }
    }

    private fun initViewModel() {

        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    try {
                        val apiError = JsonHelper.fromJson<ApiError>(it.throwable.message)
                        showErrorAndExit(message = JsonHelper.toJson(apiError.message))
                    } catch (e: Exception) {
                        handleOnError(it.throwable)
                    }
                }
                is UiState.Exit -> {
                    navigator.navigateClearStacks(
                        getAppCompatActivity(),
                        LoginActivity::class.java,
                        Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
                        true
                    )
                }
            }
        })

        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver{
            userCreationActivity.setContactInput(viewModel.input)
            navigateToOTPScreen(it)
        })

        val hasValue = userCreationActivity.viewModel.hasContactInput.hasValue()
        if (hasValue && !viewModel.isLoadedScreen.hasValue()) {
            userCreationActivity.viewModel.contactInput.let {
                viewModel.setExistingContactInfoInput(it)
            }
        } else {
            setSubmitButtonState(false)
        }
    }


    private fun initViewBinding() {
        viewModel.loadDefaultForm(userCreationActivity.getDefaultForm())
        viewModel.input.emailInput
            .subscribe {
                binding.etEmail.setText(it)
            }.addTo(disposables)
        viewModel.input.countryCodeInput
            .subscribe {
                binding.etCountryCode.setText(it)
            }.addTo(disposables)
        viewModel.input.mobileInput
            .subscribe {
                binding.etMobile.setText(it)
            }.addTo(disposables)
    }

    private fun validateForm() {
        formDisposable.clear()

        val emailObservable = RxValidator.createFor(binding.etEmail)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.etEmail.hint
                )
            )
            .email(getString(R.string.error_invalid_email_address))
            .onFocusChanged(hasSkip = false)
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val mobileNumberObservable = RxValidator.createFor(binding.etMobile)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.tvMobile.text
                )
            )
            .patternMatches(getString(R.string.error_mobile_field),
                Pattern.compile(REGEX_FORMAT_MOBILE_NUMBER_PH))
            .minLength(
                resources.getInteger(R.integer.max_length_mobile_number_ph),
                String.format(
                    getString(R.string.error_validation_min),
                    resources.getInteger(R.integer.max_length_mobile_number_ph).toString()
                )
            )
            .onFocusChanged(hasSkip = false)
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        initError(emailObservable, binding.tvEmail)
        initError(
            mobileNumberObservable,
            binding.tvMobile,
            errorTextView = binding.textViewMobileNumberError,
            textInputLayout = binding.textInputLayoutPrefix
        )

        RxCombineValidator(
            emailObservable,
            mobileNumberObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
                setSubmitButtonState(it)
            }
            .subscribe()
            .addTo(formDisposable)
    }

    private fun attemptSubmit() {
        clearFormFocus()
        if (viewModel.hasValidForm()) {
            updatePreTextValues()
            viewModel.onClickedNext(
                userCreationActivity.getDefaultForm()
            )
        } else {
            refreshFields()
        }
    }

    private fun updatePreTextValues() {
        viewModel.setPreTextValues(
            binding.etEmail.getTextNullable(),
            binding.etMobile.getTextNullable(),
            binding.etCountryCode.getTextNullable()
        )
    }

    private fun refreshFields() {
        binding.etEmail.refresh()
        binding.etMobile.refresh()
        binding.etCountryCode.refresh()
    }

    private fun initError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>,
        titleTextView: TextView,
        errorTextView: TextView? = null,
        textInputLayout: TextInputLayout? = null
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { validation ->
                viewUtil.setError(validation)

                titleTextView.setTextColor(when (validation.isProper) {
                    true -> ContextCompat.getColor(requireContext(), R.color.dsColorDarkGray)
                    else -> ContextCompat.getColor(requireContext(), R.color.colorErrorColor)
                })

                errorTextView?.let {
                    it.visibility = when (validation.isProper) {
                        true -> View.GONE
                        else -> View.VISIBLE
                    }
                    it.text = validation.message
                }

                textInputLayout?.let {
                    it.error = when (validation.isProper) {
                        true -> ""
                        else -> " "
                    }
                }
            }.addTo(formDisposable)
    }

    private fun setSubmitButtonState(it: Boolean?) {
        binding.buttonNext.isEnabled = it!!
    }

    private fun clearFormFocus() {
        binding.constraintLayout1.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            binding.constraintLayout1.requestFocus()
            binding.constraintLayout1.isFocusableInTouchMode = true
        }
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
                    //getAppCompatActivity().finish()
                }
            )
        }
    }

    private fun navigateToOTPScreen(response: Auth) {

        val action = UcEnterContactInfoFragmentDirections.actionEnterContactToOtp(
            request = JsonHelper.toJson(response),
            page = OTPActivity.PAGE_USER_CREATION,
            openAccountForm = JsonHelper.toJson(userCreationActivity.getDefaultForm())
        )

        findNavController().navigate(action)
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            userCreationActivity.popBackStack()
        }
    }

    companion object {
        const val REGEX_FORMAT_MOBILE_NUMBER_PH = "^[8-9]\\d+\$"

    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcEnterContactInfoBinding
        get() = FragmentUcEnterContactInfoBinding::inflate

    override val viewModelClassType: Class<UcEnterContactInfoViewModel>
        get() = UcEnterContactInfoViewModel::class.java
}