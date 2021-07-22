package com.unionbankph.corporate.dao.presentation.personal_info_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.databinding.FragmentDaoPersonalInformationStep2Binding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*
import javax.annotation.concurrent.ThreadSafe

class DaoPersonalInformationStepTwoFragment :
    BaseFragment<FragmentDaoPersonalInformationStep2Binding, DaoPersonalInformationStepTwoViewModel>(),
    DaoActivity.ActionEvent, ImeOptionEditText.OnImeOptionListener {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

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
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
        initCheckListener()
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
            }
        })
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            daoActivity.setPersonalInformationStepTwoInput(it)
            if (viewModel.isEditMode.value == true) {
                requireActivity().onBackPressed()
            } else {
                findNavController().navigate(R.id.action_personal_information_step_three)
            }
        })
        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver {
            navigationDaoResult(it)
        })
        if (daoActivity.viewModel.hasPersonalInformationTwoInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input2.let {
                viewModel.setExistingPersonalInformationStepTwo(it)
            }
        }
        arguments?.getBoolean(EXTRA_IS_EDIT, false)?.let {
            viewModel.isEditMode.onNext(it)
        }
    }

    private fun initBinding() {
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        viewModel.input.governmentIdNumberInput
            .subscribe {
                binding.tieGovernmentId.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.dateOfBirthInput
            .subscribe {
                binding.tieDateOfBirth.setText(it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE))
            }.addTo(disposables)
        viewModel.input.placeOfBirthInput
            .subscribe {
                binding.tiePlaceOfBirth.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.governmentIdTypeInput
            .subscribe {
                binding.tieGovernmentType.setText(it.value)
                viewUtil.setEditTextMaxLength(
                    binding.tieGovernmentType,
                    resources.getInteger(
                        if (it.value == formatString(R.string.title_sss)) {
                            R.integer.max_length_sss
                        } else {
                            R.integer.max_length_tin
                        }
                    )
                )
            }.addTo(disposables)
        viewModel.input.nationalityInput
            .subscribe {
                binding.tieNationality.setText(it.value)
            }.addTo(disposables)
        viewModel.input.usCitizenshipInput
            .subscribe {
                binding.cbUsCitizenship.isChecked = !it
                binding.tilUsRecordType.visibility(it)
                binding.tilUsRecord.visibility(it)
                if (!it) {
                    binding.tieUsRecord.setText(Constant.EMPTY)
                    binding.tieUsRecordType.setText(Constant.EMPTY)
                } else {
                    binding.tieUsRecordType.clear()
                    binding.tieUsRecord.clear()
                }
            }.addTo(disposables)
        viewModel.input.recordTypeInput
            .subscribe {
                binding.tieUsRecordType.setText(it.value)
            }.addTo(disposables)
        viewModel.input.usRecordInput
            .subscribe {
                binding.tieUsRecord.setText(it)
            }.addTo(disposables)
        viewModel.isEditMode
            .subscribe {
                if (it) {
                    daoActivity.setButtonName(formatString(R.string.action_save))
                }
            }.addTo(disposables)
    }

    private fun init() {
        initImeOption()
        validateForm()
    }

    private fun initImeOption() {
        val imeOptionEditText = ImeOptionEditText()
        imeOptionEditText.addEditText(
            binding.tiePlaceOfBirth,
            binding.tieUsRecord
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.GOVERNMENT_ID.name -> {
                    binding.tieGovernmentId.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.governmentIdTypeInput.onNext(selector)
                }
                SingleSelectorTypeEnum.NATIONALITY.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.nationalityInput.onNext(selector)
                }
                SingleSelectorTypeEnum.RECORD_TYPE.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.recordTypeInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
//        tie_government_type.setOnClickListener {
//            navigateSingleSelector(SingleSelectorTypeEnum.GOVERNMENT_ID.name)
//        }
        binding.tieDateOfBirth.setOnClickListener {
            showDatePicker(
                minDate = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
                maxDate = Calendar.getInstance(),
                calendar = viewModel.input.dateOfBirthInput.value.convertToCalendar(),
                callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    val dateString = viewUtil.getDateFormatByCalendar(
                        Calendar.getInstance().apply {
                            set(year, monthOfYear, dayOfMonth)
                        },
                        DateFormatEnum.DATE_FORMAT_DATE_SLASH.value
                    )
                    viewModel.input.dateOfBirthInput.onNext(dateString)
                }
            )
        }
        binding.tieNationality.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.NATIONALITY.name)
        }
        binding.tieUsRecordType.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.RECORD_TYPE.name)
        }
    }

    private fun initCheckListener() {
        binding.cbUsCitizenship.setOnCheckedChangeListener { _, isChecked ->
            viewModel.input.usCitizenshipInput.onNext(!isChecked)
        }
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_additional_information))
        daoActivity.setProgressValue(2)
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.setEnableButton(true)
        daoActivity.showProgress(true)
        daoActivity.setActionEvent(this)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            updateFreeTextFields()
            if (viewModel.hasFormChanged()) {
                daoActivity.showGoBackBottomSheet()
            } else {
                daoActivity.popBackStack()
            }
        }
    }

    private fun refreshFields() {
        binding.tieGovernmentId.refresh()
        binding.tieDateOfBirth.refresh()
        binding.tiePlaceOfBirth.refresh()
        binding.tieUsRecordType.refresh()
        binding.tieUsRecord.refresh()
        binding.tieNationality.refresh()
    }

    private fun validateForm() {
        val governmentIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.max_length_tin),
            maxLength = resources.getInteger(R.integer.max_length_tin),
            editText = binding.tieGovernmentId
        )
        val dateOfBirthObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieDateOfBirth
        )
        val placeOfBirthObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePlaceOfBirth
        )
        val usRecordTypeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieUsRecordType
        )
        val usRecordTypeOtherObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieUsRecord
        )
        val nationalityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieNationality
        )
        initSetError(governmentIdObservable)
        initSetError(dateOfBirthObservable)
        initSetError(placeOfBirthObservable)
        initSetError(nationalityObservable)
        initSetError(usRecordTypeObservable)
        initSetError(usRecordTypeOtherObservable)
        RxCombineValidator(
            governmentIdObservable,
            dateOfBirthObservable,
            placeOfBirthObservable,
            nationalityObservable,
            usRecordTypeObservable,
            usRecordTypeOtherObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun navigateSingleSelector(selectorType: String) {
        val action = DaoPersonalInformationStepTwoFragmentDirections.actionSelectorActivity(
            selectorType,
            true
        )
        findNavController().navigate(action)
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val action =
            DaoPersonalInformationStepTwoFragmentDirections.actionDaoResultFragment(
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

    override fun onClickNext() {
        clearFormFocus()
        if (viewModel.hasValidForm()) {
            updateFreeTextFields()
            viewModel.onClickedNext()
        } else {
            showMissingFieldDialog()
            refreshFields()
        }
    }

    private fun updateFreeTextFields() {
        viewModel.setPreTextValues(
            binding.tieGovernmentId.getTextNullable(),
            binding.tiePlaceOfBirth.getTextNullable(),
            binding.tieUsRecord.getTextNullable()
        )
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
    }

    override val viewModelClassType: Class<DaoPersonalInformationStepTwoViewModel>
        get() = DaoPersonalInformationStepTwoViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoPersonalInformationStep2Binding
        get() = FragmentDaoPersonalInformationStep2Binding::inflate
}
