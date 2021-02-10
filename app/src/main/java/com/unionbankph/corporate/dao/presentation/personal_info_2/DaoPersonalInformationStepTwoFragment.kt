package com.unionbankph.corporate.dao.presentation.personal_info_2

import android.os.Bundle
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
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_personal_information_step_2.*
import java.util.*
import javax.annotation.concurrent.ThreadSafe

class DaoPersonalInformationStepTwoFragment :
    BaseFragment<DaoPersonalInformationStepTwoViewModel>(R.layout.fragment_dao_personal_information_step_2),
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
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[DaoPersonalInformationStepTwoViewModel::class.java]
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
                tie_government_id.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.dateOfBirthInput
            .subscribe {
                tie_date_of_birth.setText(it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE))
            }.addTo(disposables)
        viewModel.input.placeOfBirthInput
            .subscribe {
                tie_place_of_birth.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.governmentIdTypeInput
            .subscribe {
                tie_government_type.setText(it.value)
                viewUtil.setEditTextMaxLength(
                    tie_government_type,
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
                tie_nationality.setText(it.value)
            }.addTo(disposables)
        viewModel.input.usCitizenshipInput
            .subscribe {
                cb_us_citizenship.isChecked = !it
                til_us_record_type.visibility(it)
                til_us_record.visibility(it)
                if (!it) {
                    tie_us_record.setText(Constant.EMPTY)
                    tie_us_record_type.setText(Constant.EMPTY)
                } else {
                    tie_us_record_type.clear()
                    tie_us_record.clear()
                }
            }.addTo(disposables)
        viewModel.input.recordTypeInput
            .subscribe {
                tie_us_record_type.setText(it.value)
            }.addTo(disposables)
        viewModel.input.usRecordInput
            .subscribe {
                tie_us_record.setText(it)
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
            tie_place_of_birth,
            tie_us_record
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.GOVERNMENT_ID.name -> {
                    tie_government_id.clear()
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
        tie_date_of_birth.setOnClickListener {
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
        tie_nationality.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.NATIONALITY.name)
        }
        tie_us_record_type.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.RECORD_TYPE.name)
        }
    }

    private fun initCheckListener() {
        cb_us_citizenship.setOnCheckedChangeListener { _, isChecked ->
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
        tie_government_id.refresh()
        tie_date_of_birth.refresh()
        tie_place_of_birth.refresh()
        tie_us_record_type.refresh()
        tie_us_record.refresh()
        tie_nationality.refresh()
    }

    private fun validateForm() {
        val governmentIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.max_length_tin),
            maxLength = resources.getInteger(R.integer.max_length_tin),
            editText = tie_government_id
        )
        val dateOfBirthObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_date_of_birth
        )
        val placeOfBirthObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_place_of_birth
        )
        val usRecordTypeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_us_record_type
        )
        val usRecordTypeOtherObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_us_record
        )
        val nationalityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_nationality
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
        constraint_layout.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            constraint_layout.requestFocus()
            constraint_layout.isFocusableInTouchMode = true
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
            tie_government_id.getTextNullable(),
            tie_place_of_birth.getTextNullable(),
            tie_us_record.getTextNullable()
        )
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
    }
}
