package com.unionbankph.corporate.account_setup.presentation.personal_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.data.PersonalInfoInput
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentAsPersonalInformationBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import com.unionbankph.corporate.user_creation.presentation.enter_contact_info.UcEnterContactInfoFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AsPersonalInformationFragment
    : BaseFragment<FragmentAsPersonalInformationBinding, AsPersonalInformationViewModel>(){

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    private val formDisposable = CompositeDisposable()

    override fun onViewModelBound() {
        super.onViewModelBound()
        initVM()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
        initViewBindings()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventListeners()
        initOnClicks()
    }

    private fun initVM() {
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
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

        viewModel.state.observe(viewLifecycleOwner, EventObserver {
            accountSetupActivity.setPersonalInfoInput(it)
            findNavController().navigate(R.id.action_address)
        })

        val hasExistingData = accountSetupActivity.viewModel.state.value?.hasPersonalInfoInput ?: false
        if (hasExistingData) {
            viewModel.populateFieldsWithExisting(
                accountSetupActivity.viewModel.state.value?.personalInfoInput ?: PersonalInfoInput()
            )
        }
    }

    private fun initEventListeners() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.GENDER.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.genderInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CIVIL_STATUS.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.civilStatusInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initOnClicks() {
        binding.buttonNext.setOnClickListener {
            attemptSubmit()
        }
        binding.editTextGender.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.GENDER.name)
        }
        binding.editTextCivilStatus.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.CIVIL_STATUS.name)
        }
        binding.editTextDOB.setOnClickListener {
            showDatePicker(
                minDate = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
                maxDate = Calendar.getInstance(),
                calendar = viewModel.input.dobInput.value.convertToCalendar(),
                callback = { _, year, monthOfYear, dayOfMonth ->
                    val dateString = viewUtil.getDateFormatByCalendar(
                        Calendar.getInstance().apply {
                            set(year, monthOfYear, dayOfMonth)
                        },
                        DateFormatEnum.DATE_FORMAT_DATE_SLASH.value
                    )
                    viewModel.input.dobInput.onNext(dateString)
                }
            )
        }
        binding.checkBoxNotUsCitizen.setOnCheckedChangeListener { _, isChecked ->
            viewModel.input.notUsCitizenInput.onNext(isChecked)
        }
    }

    private fun validateForm() {
        formDisposable.clear()
        val firstNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextFirstName
        )
        val middleNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextMiddleName
        )
        val lastNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextLastName
        )
        val mobileNumberObservable = RxValidator.createFor(binding.viewMobileNumber.etMobile)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    getString(R.string.title_mobile_number)
                )
            )
            .patternMatches(getString(R.string.error_mobile_field),
                Pattern.compile(UcEnterContactInfoFragment.REGEX_FORMAT_MOBILE_NUMBER_PH))
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
        val emailObservable = RxValidator.createFor(binding.editTextEmail)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    getString(R.string.title_email)
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
        val genderObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextGender,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.title_gender)
            )
        )
        val civilStatusObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextCivilStatus
        )
        val tinObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextTIN
        )
        val dobObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextDOB
        )
        val pobObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextPOB
        )
        val nationalityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextNationality
        )
        initError(firstNameObservable, binding.textViewFirstNameLabel)
        initError(middleNameObservable, binding.textViewMiddleName)
        initError(lastNameObservable, binding.textViewLastName)
        initError(
            mobileNumberObservable,
            binding.viewMobileNumber.textViewMobileNumberLabel,
            binding.viewMobileNumber.textViewMobileNumberError,
            binding.viewMobileNumber.textInputLayoutPrefix
        )
        initError(emailObservable, binding.tvEmail)
        initError(genderObservable, binding.textViewGender)
        initError(civilStatusObservable, binding.textViewCivilStatus)
        initError(tinObservable, binding.textViewTIN)
        initError(dobObservable, binding.textViewDOB)
        initError(pobObservable, binding.textViewPOB)
        initError(nationalityObservable, binding.textViewNationality)
        RxCombineValidator(
            firstNameObservable,
            middleNameObservable,
            lastNameObservable,
            mobileNumberObservable,
            emailObservable,
            genderObservable,
            civilStatusObservable,
            tinObservable,
            dobObservable,
            pobObservable,
            nationalityObservable,
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(formDisposable)
    }

    private fun initViewBindings() {
        viewModel.input.firstNameInput
            .subscribe {
                binding.editTextFirstName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.middleNameInput
            .subscribe {
                binding.editTextMiddleName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.lastNameInput
            .subscribe {
                binding.editTextLastName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.mobileInput
            .subscribe {
                binding.viewMobileNumber.etMobile.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.emailInput
            .subscribe {
            binding.editTextEmail.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.genderInput
            .subscribe {
                binding.editTextGender.setTextNullable(it.value?: "")
            }.addTo(disposables)
        viewModel.input.civilStatusInput
            .subscribe {
                binding.editTextCivilStatus.setTextNullable(it.value?: "")
            }.addTo(disposables)
        viewModel.input.tinInput
            .subscribe {
                binding.editTextTIN.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.dobInput
            .subscribe {
                binding.editTextDOB.setText(
                    it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                )
            }.addTo(disposables)
        viewModel.input.pobInput
            .subscribe {
                binding.editTextPOB.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.nationalityInput
            .subscribe {
                binding.editTextNationality.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.notUsCitizenInput
            .subscribe {
                binding.checkBoxNotUsCitizen.isChecked = it
            }.addTo(disposables)
    }

    private fun attemptSubmit() {
        if (viewModel.hasValidForm()) {
            syncInputs()
            viewModel.onClickedNext()
        } else {
            refreshFields()
        }
    }

    private fun initError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>,
        labelTextView: TextView,
        errorTextView: TextView? = null,
        textInputLayout: TextInputLayout? = null
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { validation ->
                viewUtil.setError(validation)
                labelTextView.setTextColor(when (validation.isProper) {
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

    private fun refreshFields() {
        binding.apply {
            editTextFirstName.refresh()
            editTextMiddleName.refresh()
            editTextLastName.refresh()
            viewMobileNumber.etMobile.refresh()
            editTextEmail.refresh()
            editTextGender.refresh()
            editTextCivilStatus.refresh()
            editTextTIN.refresh()
            editTextDOB.refresh()
            editTextPOB.refresh()
            editTextNationality.refresh()
        }
    }

    private fun syncInputs() {
        binding.apply {
            viewModel.syncInputData(
                firstNameInput = editTextFirstName.getTextNullable(),
                middleNameInput = editTextMiddleName.getTextNullable(),
                lastNameInput = editTextLastName.getTextNullable(),
                mobileInput = viewMobileNumber.etMobile.getTextNullable(),
                emailInput = editTextEmail.getTextNullable(),
                genderInput = viewModel.input.genderInput.value,
                civilStatusInput = viewModel.input.civilStatusInput.value,
                tinInput = editTextTIN.getTextNullable(),
                dobInput = viewModel.input.dobInput.value,
                pobInput = editTextPOB.getTextNullable(),
                nationalityInput = editTextNationality.getTextNullable(),
                isNotUsCitizen = viewModel.input.notUsCitizenInput.value!!
            )
        }
    }

    private fun navigateSingleSelector(selectorType: String) {
        val action = AsPersonalInformationFragmentDirections.actionSelectorActivity(
            selectorType,
            true
        )
        findNavController().navigate(action)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsPersonalInformationBinding
        get() = FragmentAsPersonalInformationBinding::inflate

    override val viewModelClassType: Class<AsPersonalInformationViewModel>
        get() = AsPersonalInformationViewModel::class.java
}