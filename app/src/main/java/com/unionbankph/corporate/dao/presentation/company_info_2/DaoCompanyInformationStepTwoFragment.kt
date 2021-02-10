package com.unionbankph.corporate.dao.presentation.company_info_2

import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.clear
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.refresh
import com.unionbankph.corporate.app.common.extension.setContextCompatBackground
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_company_information_step_2.*
import java.util.*
import javax.annotation.concurrent.ThreadSafe

class DaoCompanyInformationStepTwoFragment :
    BaseFragment<DaoCompanyInformationStepTwoViewModel>(R.layout.fragment_dao_company_information_step_2),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[DaoCompanyInformationStepTwoViewModel::class.java]
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
    }

    private fun initBinding() {
        viewModel.typeOfOfficeInput
            .subscribe {
                setOtherTextInputEditText(
                    it.value,
                    til_other_type_of_office,
                    tie_other_type_of_office
                )
                tie_type_of_office.setText(it.value)
            }.addTo(disposables)
        viewModel.businessOwnershipInput
            .subscribe {
                setOtherTextInputEditText(
                    it.value,
                    til_other_business_ownership,
                    tie_other_business_ownership
                )
                tie_business_ownership.setText(it.value)
            }.addTo(disposables)
        viewModel.sourceOfFundsInput
            .subscribe {
                setOtherTextInputEditText(
                    it.value,
                    til_other_source_of_funds,
                    tie_other_source_of_funds
                )
                tie_source_of_funds.setText(it.value)
            }.addTo(disposables)
        viewModel.purposeInput
            .subscribe {
                tie_purpose.setText(it.value)
            }.addTo(disposables)
        viewModel.dateOfIncorporationInput
            .subscribe {
                tie_date_of_incorporation.setText(
                    viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }.addTo(disposables)
    }

    private fun init() {
        til_other_type_of_office.visibility(false)
        til_other_business_ownership.visibility(false)
        til_other_source_of_funds.visibility(false)
        tie_other_type_of_office.setText(Constant.EMPTY)
        tie_other_business_ownership.setText(Constant.EMPTY)
        tie_other_source_of_funds.setText(Constant.EMPTY)
        validateForm()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.TYPE_OF_OFFICE.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.typeOfOfficeInput.onNext(selector)
                }
                SingleSelectorTypeEnum.BUSINESS_OWNERSHIP.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.businessOwnershipInput.onNext(selector)
                }
                SingleSelectorTypeEnum.SOURCE_OF_FUNDS.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.sourceOfFundsInput.onNext(selector)
                }
                SingleSelectorTypeEnum.ESTIMATED_AMOUNT.name -> {
                    val selector =
                        JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.estimatedAmountInput.onNext(selector)
                }
                SingleSelectorTypeEnum.PURPOSE.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.purposeInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        tie_type_of_office.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.TYPE_OF_OFFICE.name)
        }
        tie_business_ownership.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.BUSINESS_OWNERSHIP.name)
        }
        tie_date_of_incorporation.setOnClickListener {
            showDatePicker(
                minDate = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
                maxDate = Calendar.getInstance(),
                callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    viewModel.dateOfIncorporationInput.onNext(
                        Calendar.getInstance().apply {
                            set(year, monthOfYear, dayOfMonth)
                        }
                    )
                }
            )
        }
        tie_source_of_funds.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.SOURCE_OF_FUNDS.name)
        }
        tie_purpose.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PURPOSE.name)
        }
        sb_estimated_amount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val estimatedAmounts =
                    resources.getStringArray(R.array.array_estimated_amount).toMutableList()
                val selectedAmount = estimatedAmounts[progress]
                tv_amount.text =
                    ("Around ${autoFormatUtil.formatWithTwoDecimalPlaces(selectedAmount, "PHP")}")
                if (progress == 0) {
                    tie_estimated_amount.text?.clear()
                    view_border_estimated_amount.setContextCompatBackground(R.drawable.bg_cardview_border_red)
                } else {
                    tie_estimated_amount.setText(selectedAmount)
                    view_border_estimated_amount.setContextCompatBackground(null)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_company_information))
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(6)
    }

    private fun refreshFields() {
        tie_type_of_office.refresh()
        tie_business_ownership.refresh()
        tie_country.refresh()
        tie_date_of_incorporation.refresh()
        tie_source_of_funds.refresh()
        tie_other_source_of_funds.refresh()
        tie_purpose.refresh()
        tie_estimated_amount.refresh()
        if (tie_estimated_amount.length() == 0) {
            view_border_estimated_amount.setContextCompatBackground(R.drawable.bg_cardview_border_red)
        } else {
            view_border_estimated_amount.setContextCompatBackground(null)
        }
    }

    private fun validateForm() {
        val typeOfBusinessObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_type_of_office
        )
        val typeOfBusinessOtherObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_other_type_of_office
        )
        val businessOwnershipObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_business_ownership
        )
        val businessOwnershipOtherObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_other_business_ownership
        )
        val countryObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_country
        )
        val dateOfIncorporationObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_date_of_incorporation
        )
        val sourceOfFundsObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_source_of_funds
        )
        val otherSourceOfFundsObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_other_source_of_funds
        )
        val purposeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_purpose
        )
        val estimatedAmountObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_estimated_amount
        )
        initSetError(typeOfBusinessObservable)
        initSetError(typeOfBusinessOtherObservable)
        initSetError(businessOwnershipObservable)
        initSetError(businessOwnershipOtherObservable)
        initSetError(countryObservable)
        initSetError(dateOfIncorporationObservable)
        initSetError(sourceOfFundsObservable)
        initSetError(otherSourceOfFundsObservable)
        initSetError(purposeObservable)
        initSetError(estimatedAmountObservable)
        RxCombineValidator(
            typeOfBusinessObservable,
            typeOfBusinessOtherObservable,
            businessOwnershipObservable,
            businessOwnershipOtherObservable,
            countryObservable,
            dateOfIncorporationObservable,
            sourceOfFundsObservable,
            otherSourceOfFundsObservable,
            purposeObservable,
            estimatedAmountObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun setOtherTextInputEditText(
        dropDownValue: String?,
        til: TextInputLayout,
        tie: TextInputEditText
    ) {
        if (OTHER == dropDownValue) {
            til.visibility(true)
            tie.clear()
        } else {
            til.visibility(false)
            tie.setText(Constant.EMPTY)
        }
    }

    private fun navigateSingleSelector(selectorType: String) {
        val action =
            DaoCompanyInformationStepTwoFragmentDirections.actionSelectorActivity(selectorType)
        findNavController().navigate(action)
    }

    private fun clearFormFocus() {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        constraint_layout.requestFocus()
        constraint_layout.isFocusableInTouchMode = true
    }

    override fun onClickNext() {
        if (viewModel.hasValidForm()) {
            clearFormFocus()
            findNavController().navigate(R.id.action_preferred_branch)
        } else {
            showMissingFieldDialog()
            refreshFields()
        }
    }

    @ThreadSafe
    companion object {
        const val OTHER = "Other"
    }
}
