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
import com.unionbankph.corporate.databinding.FragmentDaoCompanyInformationStep2Binding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*
import javax.annotation.concurrent.ThreadSafe

class DaoCompanyInformationStepTwoFragment :
    BaseFragment<FragmentDaoCompanyInformationStep2Binding, DaoCompanyInformationStepTwoViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

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
                    binding.tilOtherTypeOfOffice,
                    binding.tieOtherTypeOfOffice
                )
                binding.tieOtherTypeOfOffice.setText(it.value)
            }.addTo(disposables)
        viewModel.businessOwnershipInput
            .subscribe {
                setOtherTextInputEditText(
                    it.value,
                    binding.tilOtherBusinessOwnership,
                    binding.tieOtherBusinessOwnership
                )
                binding.tieBusinessOwnership.setText(it.value)
            }.addTo(disposables)
        viewModel.sourceOfFundsInput
            .subscribe {
                setOtherTextInputEditText(
                    it.value,
                    binding.tilOtherSourceOfFunds,
                    binding.tieOtherSourceOfFunds
                )
                binding.tieSourceOfFunds.setText(it.value)
            }.addTo(disposables)
        viewModel.purposeInput
            .subscribe {
                binding.tiePurpose.setText(it.value)
            }.addTo(disposables)
        viewModel.dateOfIncorporationInput
            .subscribe {
                binding.tieDateOfIncorporation.setText(
                    viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }.addTo(disposables)
    }

    private fun init() {
        binding.tilOtherTypeOfOffice.visibility(false)
        binding.tilOtherBusinessOwnership.visibility(false)
        binding.tilOtherSourceOfFunds.visibility(false)
        binding.tieOtherTypeOfOffice.setText(Constant.EMPTY)
        binding.tieOtherBusinessOwnership.setText(Constant.EMPTY)
        binding.tieOtherSourceOfFunds.setText(Constant.EMPTY)
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
        binding.tieTypeOfOffice.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.TYPE_OF_OFFICE.name)
        }
        binding.tieBusinessOwnership.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.BUSINESS_OWNERSHIP.name)
        }
        binding.tieDateOfIncorporation.setOnClickListener {
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
        binding.tieSourceOfFunds.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.SOURCE_OF_FUNDS.name)
        }
        binding.tiePurpose.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PURPOSE.name)
        }
        binding.sbEstimatedAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val estimatedAmounts =
                    resources.getStringArray(R.array.array_estimated_amount).toMutableList()
                val selectedAmount = estimatedAmounts[progress]
                binding.tvAmount.text =
                    ("Around ${autoFormatUtil.formatWithTwoDecimalPlaces(selectedAmount, "PHP")}")
                if (progress == 0) {
                    binding.tieEstimatedAmount.text?.clear()
                    binding.viewBorderEstimatedAmount.setContextCompatBackground(R.drawable.bg_cardview_border_red)
                } else {
                    binding.tieEstimatedAmount.setText(selectedAmount)
                    binding.viewBorderEstimatedAmount.setContextCompatBackground(null)
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
        binding.tieTypeOfOffice.refresh()
        binding.tieBusinessOwnership.refresh()
        binding.tieCountry.refresh()
        binding.tieDateOfIncorporation.refresh()
        binding.tieSourceOfFunds.refresh()
        binding.tieOtherSourceOfFunds.refresh()
        binding.tiePurpose.refresh()
        binding.tieEstimatedAmount.refresh()
        if (binding.tieEstimatedAmount.length() == 0) {
            binding.viewBorderEstimatedAmount.setContextCompatBackground(R.drawable.bg_cardview_border_red)
        } else {
            binding.viewBorderEstimatedAmount.setContextCompatBackground(null)
        }
    }

    private fun validateForm() {
        val typeOfBusinessObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieTypeOfOffice
        )
        val typeOfBusinessOtherObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieOtherTypeOfOffice
        )
        val businessOwnershipObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieBusinessOwnership
        )
        val businessOwnershipOtherObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieOtherBusinessOwnership
        )
        val countryObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieCountry
        )
        val dateOfIncorporationObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieDateOfIncorporation
        )
        val sourceOfFundsObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieSourceOfFunds
        )
        val otherSourceOfFundsObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieOtherSourceOfFunds
        )
        val purposeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePurpose
        )
        val estimatedAmountObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieEstimatedAmount
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
        binding.constraintLayout.requestFocus()
        binding.constraintLayout.isFocusableInTouchMode = true
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

    override val layoutId: Int
        get() = R.layout.fragment_dao_company_information_step_2

    override val viewModelClassType: Class<DaoCompanyInformationStepTwoViewModel>
        get() = DaoCompanyInformationStepTwoViewModel::class.java
}
