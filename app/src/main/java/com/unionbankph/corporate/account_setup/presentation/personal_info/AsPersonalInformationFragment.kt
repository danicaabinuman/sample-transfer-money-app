package com.unionbankph.corporate.account_setup.presentation.personal_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.data.GenderEnum
import com.unionbankph.corporate.account_setup.data.PersonalInformation
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
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
    : BaseFragment<FragmentAsPersonalInformationBinding, AsPersonalInformationViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            setIsScreenScrollable(false)
            setToolbarButtonType(AccountSetupActivity.BUTTON_SAVE_EXIT)
            showToolbarButton(true)
            showProgress(true)
            setProgressValue(6)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initVM()
    }

    override fun onViewsBound() {
        super.onViewsBound()

        binding.cbAsPersonalInfoNotUsCitizen.setMSMETheme()

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

        val hasExistingData =
            accountSetupActivity.viewModel.state.value?.hasPersonalInfoInput ?: false

        if (hasExistingData) {
            viewModel.populateFieldsWithExisting(
                accountSetupActivity.viewModel.state.value?.personalInformation ?: PersonalInformation()
            )
        }
    }

    private fun initEventListeners() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
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
        binding.includeAsPersonalInfoGender.rbWidgetGenderSelectionMale.setOnClickListener {
            viewModel.input.genderInput.onNext(GenderEnum.MALE)
        }
        binding.includeAsPersonalInfoGender.rbWidgetGenderSelectionFemale.setOnClickListener {
            viewModel.input.genderInput.onNext(GenderEnum.FEMALE)
        }
        binding.tieAsPersonalInfoCivilStatus.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.CIVIL_STATUS.name)
        }
        binding.tieAsPersonalInfoDob.setOnClickListener {
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
        binding.cbAsPersonalInfoNotUsCitizen.setOnCheckedChangeListener { _, isChecked ->
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
            editText = binding.tieAsPersonalInfoFirsName
        )
        val middleNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsPersonalInfoMiddleName
        )
        val lastNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsPersonalInfoLastName
        )
        val mobileNumberObservable = RxValidator.createFor(
            binding.includeAsPersonalInfoMobile.tieMobileNumber)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    getString(R.string.title_mobile_number)
                )
            )
            .patternMatches(
                getString(R.string.error_mobile_field),
                Pattern.compile(UcEnterContactInfoFragment.REGEX_FORMAT_MOBILE_NUMBER_PH)
            )
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
        val emailObservable = RxValidator.createFor(binding.tieAsPersonalInfoEmail)
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
            editText = binding.includeAsPersonalInfoGender.tieWidgetGenderSelection,
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
            editText = binding.tieAsPersonalInfoCivilStatus
        )
        val tinObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.max_length_sss),
            maxLength = resources.getInteger(R.integer.max_length_sss),
            editText = binding.tieAsPersonalInfoTin
        )
        val dobObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsPersonalInfoDob
        )
        val pobObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsPersonalInfoPob
        )
        val nationalityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsPersonalInfoNationality
        )
        initError(firstNameObservable, binding.tvAsPersonalInfoFirstName)
        initError(middleNameObservable, binding.tvAsPersonalInfoMiddleName)
        initError(lastNameObservable, binding.tvAsPersonalInfoLastName)
        initError(
            mobileNumberObservable,
            binding.includeAsPersonalInfoMobile.tvMobileNumberLabel,
            binding.includeAsPersonalInfoMobile.tvMobileFieldError,
            binding.includeAsPersonalInfoMobile.tilMobilePrefix
        )
        initError(emailObservable, binding.tvAsPersonalInfoEmailAddress)
        initError(
            genderObservable,
            binding.includeAsPersonalInfoGender.tvWidgetGenderSelectionLabel,
            binding.includeAsPersonalInfoGender.tvWidgetGenderSelectionError,
            binding.includeAsPersonalInfoGender.tilWidgetGenderSelection,
            binding.includeAsPersonalInfoGender.vWidgetGenderSelectionDividerBorder,
            binding.includeAsPersonalInfoGender.clWidgetGenderSelectionContainer
        )
        initError(civilStatusObservable, binding.tvAsPersonalInfoCivilStatus)
        initError(tinObservable, binding.tvAsPersonalInfoTin)
        initError(dobObservable, binding.tvAsPersonalInfoDob)
        initError(pobObservable, binding.tvAsPersonalInfoPob)
        initError(nationalityObservable, binding.tvAsPersonalInfoNationality)
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
                binding.tieAsPersonalInfoFirsName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.middleNameInput
            .subscribe {
                binding.tieAsPersonalInfoMiddleName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.lastNameInput
            .subscribe {
                binding.tieAsPersonalInfoLastName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.mobileInput
            .subscribe {
                binding.includeAsPersonalInfoMobile.tieMobileNumber.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.emailInput
            .subscribe {
                binding.tieAsPersonalInfoEmail.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.genderInput
            .subscribe {
                setSelectedGender(it)
            }.addTo(disposables)
        viewModel.input.civilStatusInput
            .subscribe {
                binding.tieAsPersonalInfoCivilStatus.setTextNullable(it.value ?: "")
            }.addTo(disposables)
        viewModel.input.tinInput
            .subscribe {
                binding.tieAsPersonalInfoTin.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.dobInput
            .subscribe {
                binding.tieAsPersonalInfoDob.setText(
                    it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                )
            }.addTo(disposables)
        viewModel.input.pobInput
            .subscribe {
                binding.tieAsPersonalInfoPob.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.nationalityInput
            .subscribe {
                binding.tieAsPersonalInfoNationality.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.notUsCitizenInput
            .subscribe {
                binding.cbAsPersonalInfoNotUsCitizen.isChecked = it
            }.addTo(disposables)
    }

    private fun setSelectedGender(it: GenderEnum?) {
        binding.includeAsPersonalInfoGender.apply {
            rbWidgetGenderSelectionMale.isChecked = it == GenderEnum.MALE
            rbWidgetGenderSelectionFemale.isChecked = it == GenderEnum.FEMALE
            tieWidgetGenderSelection.setTextNullable(it?.value!!)
        }
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
        textInputLayout: TextInputLayout? = null,
        divider: View? = null,
        container: ConstraintLayout? = null
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { validation ->
                viewUtil.setError(validation)

                labelTextView.setFieldLabelError(validation.isProper)

                errorTextView?.setFieldFooterError(validation)

                textInputLayout?.setBlankError(validation.isProper)

                divider?.setGenderDividerColor(validation.isProper)

                container?.setGenderContainerBackground(validation.isProper)

            }.addTo(formDisposable)
    }

    private fun refreshFields() {
        binding.apply {
            tieAsPersonalInfoFirsName.refresh()
            tieAsPersonalInfoMiddleName.refresh()
            tieAsPersonalInfoLastName.refresh()
            includeAsPersonalInfoMobile.tieMobileNumber.refresh()
            tieAsPersonalInfoEmail.refresh()
            includeAsPersonalInfoGender.tieWidgetGenderSelection.refresh()
            tieAsPersonalInfoCivilStatus.refresh()
            tieAsPersonalInfoTin.refresh()
            tieAsPersonalInfoDob.refresh()
            tieAsPersonalInfoPob.refresh()
            tieAsPersonalInfoNationality.refresh()
        }
    }

    private fun syncInputs() {
        binding.apply {
            viewModel.syncInputData(
                firstNameInput = tieAsPersonalInfoFirsName.getTextNullable(),
                middleNameInput = tieAsPersonalInfoMiddleName.getTextNullable(),
                lastNameInput = tieAsPersonalInfoLastName.getTextNullable(),
                mobileInput = includeAsPersonalInfoMobile.tieMobileNumber.getTextNullable(),
                emailInput = tieAsPersonalInfoEmail.getTextNullable(),
                genderInput = viewModel.input.genderInput.value,
                civilStatusInput = viewModel.input.civilStatusInput.value,
                tinInput = tieAsPersonalInfoTin.getTextNullable(),
                dobInput = viewModel.input.dobInput.value,
                pobInput = tieAsPersonalInfoPob.getTextNullable(),
                nationalityInput = tieAsPersonalInfoNationality.getTextNullable(),
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