package com.unionbankph.corporate.dao.presentation.personal_info_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.confirmation.DaoConfirmationFragmentDirections
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.databinding.FragmentDaoPersonalInformationStep1Binding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.annotation.concurrent.ThreadSafe

class DaoPersonalInformationStepOneFragment :
    BaseFragment<FragmentDaoPersonalInformationStep1Binding, DaoPersonalInformationStepOneViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    private var cancelBottomSheet: ConfirmationBottomSheet? = null

    private val formDisposable = CompositeDisposable()

    private var isFirstNameMismatched = false
    private var isLastNameMismatched = false

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
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
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            daoActivity.setPersonalInformationStepOneInput(it)
            if (viewModel.isEditMode.value == true) {
                val isBirthDateMismatch = arguments?.getBoolean(EXTRA_IS_BIRTH_DATE_MISMATCH, false)!!
                if (isBirthDateMismatch) {
                    val action = DaoPersonalInformationStepOneFragmentDirections
                        .actionPersonalInformationStepTwoHasMismatch()
                    findNavController().navigate(action)
                } else {
                    requireActivity().onBackPressed()
                }
            } else {
                findNavController().navigate(R.id.action_personal_information_step_two)
            }
        })
        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver {
            navigationDaoResult(it)
        })
        viewModel.hasValidationError.observe(this, EventObserver { results ->
            results.emailAddressErrorMessage?.let {
                viewUtil.setError(
                    RxValidationResult.createImproper(
                        binding.tieEmailAddress,
                        it.notNullable()
                    )
                )
            }
            results.mobileNumberErrorMessage?.let {
                viewUtil.setError(
                    RxValidationResult.createImproper(
                        binding.tieBusinessMobileNumber,
                        it.notNullable()
                    )
                )
            }
        })
        val hasValue = daoActivity.viewModel.hasPersonalInformationOneInput.hasValue()
        if (hasValue && !viewModel.isLoadedScreen.hasValue()) {
            daoActivity.viewModel.input1.let {
                viewModel.setExistingPersonalInformationStepOne(it)
            }
        }
        arguments?.getBoolean(EXTRA_IS_EDIT, false)?.let {
            viewModel.isEditMode.onNext(it)
        }

        isFirstNameMismatched = arguments?.getBoolean(EXTRA_IS_FIRST_NAME_MISMATCH, false)!!
        isLastNameMismatched = arguments?.getBoolean(EXTRA_IS_LAST_NAME_MISMATCH, false)!!
    }

    private fun initBinding() {
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        viewModel.input.salutationInput
            .subscribe {
                binding.tieSalutation.setText(it.value)
            }.addTo(disposables)
        viewModel.input.firstNameInput
            .subscribe {
                binding.tieFirstName.setText(it)
            }.addTo(disposables)
        viewModel.input.middleNameInput
            .subscribe {
                binding.tieMiddleName.setText(it)
            }.addTo(disposables)
        viewModel.input.lastNameInput
            .subscribe {
                binding.tieLastName.setText(it)
            }.addTo(disposables)
        viewModel.input.emailAddressInput
            .subscribe {
                binding.tieEmailAddress.setText(it)
            }.addTo(disposables)
        viewModel.input.countryCodeInput
            .subscribe {
                showCountryDetails(it)
            }.addTo(disposables)
        viewModel.input.businessMobileNumberInput
            .subscribe {
                binding.tieBusinessMobileNumber.setText(it)
            }.addTo(disposables)
        viewModel.input.genderInput
            .subscribe {
                binding.tieGender.setText(it.value)
            }.addTo(disposables)
        viewModel.input.civilStatusInput
            .subscribe {
                binding.tieCivilStatus.setText(it.value)
            }.addTo(disposables)
        viewModel.isEditMode
            .subscribe {
                if (it) {
                    daoActivity.setButtonName(formatString(R.string.action_save))
                } else {
                    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                        showExitDaoBottomSheet()
                    }
                }
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.SALUTATION.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.salutationInput.onNext(selector)
                }
                SingleSelectorTypeEnum.GENDER.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.genderInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CIVIL_STATUS.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.civilStatusInput.onNext(selector)
                }
                InputSyncEvent.ACTION_INPUT_COUNTRY -> {
                    val countryCode = JsonHelper.fromJson<CountryCode>(it.payload)
                    viewModel.input.countryCodeInput.onNext(countryCode)
                }
            }
        }.addTo(disposables)
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_PLOT_SIGNATORY_DETAILS) {
                viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
                val hasValue = daoActivity.viewModel.hasPersonalInformationOneInput.hasValue()
                if (hasValue && !viewModel.isLoadedScreen.hasValue()) {
                    daoActivity.viewModel.input1.let {
                        viewModel.setExistingPersonalInformationStepOne(it)
                    }
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        binding.tieSalutation.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.SALUTATION.name)
        }
        binding.tieGender.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.GENDER.name)
        }
        binding.tieCivilStatus.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.CIVIL_STATUS.name)
        }
        binding.viewCountryCode.tieCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_country_activity)
        }
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_personal_information))
        daoActivity.setProgressValue(1)
        daoActivity.showToolBarDetails()
        daoActivity.showProgress(true)
        daoActivity.showButton(true)
        daoActivity.setEnableButton(true)
        daoActivity.setActionEvent(this)
    }

    private fun refreshFields() {
        binding.tieSalutation.refresh()
        binding.tieFirstName.refresh()
        binding.tieMiddleName.refresh()
        binding.tieLastName.refresh()
        binding.tieEmailAddress.refresh()
        binding.tieBusinessMobileNumber.refresh()
        binding.tieGender.refresh()
        binding.tieCivilStatus.refresh()
    }

    private fun refreshClearFields() {
        binding.tieSalutation.refreshClear()
        binding.tieFirstName.refreshClear()
        binding.tieMiddleName.refreshClear()
        binding.tieLastName.refreshClear()
        binding.tieEmailAddress.refreshClear()
        binding.tieBusinessMobileNumber.refreshClear()
        binding.tieGender.refreshClear()
        binding.tieCivilStatus.refreshClear()
    }

    private fun validateForm(isCountryPH: Boolean) {
        formDisposable.clear()
        val salutationObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieSalutation
        )

        val firstNameObservable = when (isFirstNameMismatched) {
            true -> nameMismatchValidationObservable(
                binding.tieFirstName,
                viewModel.input.firstNameInput.value!!
            )
            else -> nameDefaultObservable(binding.tieFirstName)
        }

        val lastNameObservable = when (isLastNameMismatched) {
            true -> nameMismatchValidationObservable(
                binding.tieLastName,
                viewModel.input.lastNameInput.value!!
            )
            else -> nameDefaultObservable(binding.tieLastName)
        }

        val emailAddressObservable = RxValidator.createFor(binding.tieEmailAddress)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.tieEmailAddress.hint
                )
            )
            .email(getString(R.string.error_invalid_email_address))
            .onFocusChanged()
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }
        val businessMobileNumberObservable =
            if (isCountryPH) {
                RxValidator.createFor(binding.tieBusinessMobileNumber)
                    .nonEmpty(
                        String.format(
                            getString(R.string.error_specific_field),
                            binding.tieBusinessMobileNumber.hint
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
                    editText = binding.tieBusinessMobileNumber
                )
            }
        val civilStatusObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieCivilStatus
        )
        val genderObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieGender
        )
        initError(salutationObservable)
        initError(firstNameObservable)
        initError(lastNameObservable)
        initError(emailAddressObservable)
        initError(businessMobileNumberObservable)
        initError(civilStatusObservable)
        initError(genderObservable)
        RxCombineValidator(
            salutationObservable,
            firstNameObservable,
            lastNameObservable,
            emailAddressObservable,
            businessMobileNumberObservable,
            civilStatusObservable,
            genderObservable
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

    private fun nameDefaultObservable(editTextField: EditText) : Observable<RxValidationResult<EditText>> {
        return viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = editTextField
        )
    }

    private fun nameMismatchValidationObservable(editTextField: EditText, compareToString: String)
            : Observable<RxValidationResult<EditText>> {

        return RxValidator.createFor(editTextField)
            .nonEmpty(getString(R.string.error_this_field))
            .notSameAs(compareToString, getString(R.string.dao_title_mismatch_info))
            .onFocusChanged()
            .onValueChanged()
            .minLength(resources.getInteger(R.integer.min_length_field))
            .maxLength(resources.getInteger(R.integer.max_length_field_100))
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }
    }

    private fun initError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                viewUtil.setError(it)
            }.addTo(formDisposable)
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun showCountryDetails(countryCode: CountryCode?) {
        validateForm(Constant.getDefaultCountryCode().code == countryCode?.code)
        binding.tieBusinessMobileNumber.clear()
        viewUtil.setEditTextMaxLength(
            binding.tieBusinessMobileNumber,
            resources.getInteger(
                if (Constant.getDefaultCountryCode().code == countryCode?.code) {
                    R.integer.max_length_mobile_number_ph
                } else {
                    R.integer.max_length_mobile_number
                }
            )
        )
        binding.viewCountryCode.tieCountryCode.setText(countryCode?.callingCode)
        binding.viewCountryCode.imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${countryCode?.code?.toLowerCase()}")
        )
        refreshClearFields()
    }

    private fun navigateSingleSelector(selectorType: String) {
        val action = DaoPersonalInformationStepOneFragmentDirections.actionSelectorActivity(
            selectorType,
            true
        )
        findNavController().navigate(action)
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val action =
            DaoPersonalInformationStepOneFragmentDirections.actionDaoResultFragment(
                daoHit.referenceNumber,
                DaoResultFragment.TYPE_REACH_OUT_HIT,
                daoHit.businessName,
                daoHit.preferredBranch,
                daoHit.preferredBranchEmail
            )
        findNavController().navigate(action)
    }

    private fun clearFormFocus() {
        binding.constraintLayout.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            binding.constraintLayout.requestFocus()
            binding.constraintLayout.isFocusableInTouchMode = true
        }
    }

    private fun showExitDaoBottomSheet() {
        cancelBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_exit_dao),
            formatString(R.string.msg_exit_dao),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        cancelBottomSheet?.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet?.dismiss()
                viewModel.clearDaoCache()
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet?.dismiss()
            }
        })
        cancelBottomSheet?.show(
            childFragmentManager,
            TAG_CANCEL_DAO_DIALOG
        )
    }

    override fun onClickNext() {
        clearFormFocus()
        if (viewModel.hasValidForm()) {
            viewModel.setPreTextValues(
                binding.tieFirstName.getTextNullable(),
                binding.tieMiddleName.getTextNullable(),
                binding.tieLastName.getTextNullable(),
                binding.tieEmailAddress.getTextNullable(),
                binding.tieBusinessMobileNumber.getTextNullable()
            )
            viewModel.onClickedNext()
        } else {
            showMissingFieldDialog()
            refreshFields()
        }
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
        const val EXTRA_IS_FIRST_NAME_MISMATCH = "isFirstNameMismatched"
        const val EXTRA_IS_LAST_NAME_MISMATCH = "isLastNameMismatched"
        const val EXTRA_IS_BIRTH_DATE_MISMATCH = "isBirthDateMismatched"
        const val TAG_CANCEL_DAO_DIALOG = "cancel_dao"
    }

    override val viewModelClassType: Class<DaoPersonalInformationStepOneViewModel>
        get() = DaoPersonalInformationStepOneViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoPersonalInformationStep1Binding
        get() = FragmentDaoPersonalInformationStep1Binding::inflate
}
