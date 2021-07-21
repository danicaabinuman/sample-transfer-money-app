package com.unionbankph.corporate.dao.presentation.company_info_1

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.refresh
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoCompanyInformationStep1Binding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class DaoCompanyInformationStepOneFragment :
    BaseFragment<FragmentDaoCompanyInformationStep1Binding, DaoCompanyInformationStepOneViewModel>(),
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

    override fun onClickNext() {
        if (viewModel.hasValidForm()) {
            clearFormFocus()
            findNavController().navigate(R.id.action_company_information_step_two)
        } else {
            showMissingFieldDialog()
            refreshFields()
        }
    }

    private fun refreshFields() {
        binding.tieBusinessName.refresh()
        binding.tieTradeName.refresh()
        binding.tieGovernmentType.refresh()
        binding.tieGovernmentId.refresh()
        binding.tieMobileNumber.refresh()
        binding.tieBusinessAddress.refresh()
        binding.tieBarangay.refresh()
        binding.tieCity.refresh()
        binding.tieProvince.refresh()
        binding.tieZipCode.refresh()
        binding.tieHouseNo.refresh()
        binding.tieStreet.refresh()
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_company_information))
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(5)
    }

    private fun initBinding() {
        viewModel.governmentIdTypeInput
            .subscribe {
                binding.tieGovernmentId.text?.clear()
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
        viewModel.cityInput
            .subscribe {
                binding.tieCity.setText(it.value)
            }.addTo(disposables)
        viewModel.provinceInput
            .subscribe {
                binding.tieProvince.setText(it.value)
            }.addTo(disposables)
        viewModel.barangayInput
            .subscribe {
                binding.tieBarangay.setText(it.value)
            }.addTo(disposables)
        viewModel.countryCodeInput
            .subscribe {
                showCountryDetails(it)
            }.addTo(disposables)
    }

    private fun init() {
        validateForm()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.GOVERNMENT_ID.name -> {
                    val selector =
                        JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.governmentIdTypeInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY.name -> {
                    val selector =
                        JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.cityInput.onNext(selector)
                }
                SingleSelectorTypeEnum.BARANGAY.name -> {
                    val selector =
                        JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.barangayInput.onNext(selector)
                }
                SingleSelectorTypeEnum.PROVINCE.name -> {
                    val selector =
                        JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.provinceInput.onNext(selector)
                }
                InputSyncEvent.ACTION_INPUT_COUNTRY -> {
                    val countryCode =
                        JsonHelper.fromJson<CountryCode>(it.payload)
                    viewModel.countryCodeInput.onNext(countryCode)
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        binding.tieGovernmentType.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.GOVERNMENT_ID.name)
        }
        binding.tieBarangay.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.BARANGAY.name)
        }
        binding.tieCity.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.CITY.name)
        }
        binding.tieProvince.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE.name)
        }
        binding.viewCountryCode.tieCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_country_activity)
        }
    }

    private fun validateForm() {
        val tieBusinessNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieBusinessName
        )
        val tieTradeNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieTradeName
        )
        val tieGovernmentIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieGovernmentId
        )
        val tieMobileNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieMobileNumber
        )
        val tieBusinessAddressObservable = RxValidator.createFor(binding.tieBusinessAddress)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.tieBusinessAddress.hint
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
        val tieBarangayObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieBarangay
        )
        val tieCityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieCity
        )
        val tieProvinceObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieProvince
        )
        val tieZipCodeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieZipCode
        )
        val houseNoObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieHouseNo
        )
        val streetObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieStreet
        )
        initSetError(tieBusinessNameObservable)
        initSetError(tieTradeNameObservable)
        initSetError(tieGovernmentIdObservable)
        initSetError(tieMobileNumberObservable)
        initSetError(tieBusinessAddressObservable)
        initSetError(tieBarangayObservable)
        initSetError(tieCityObservable)
        initSetError(tieProvinceObservable)
        initSetError(tieZipCodeObservable)
        initSetError(houseNoObservable)
        initSetError(streetObservable)
        RxCombineValidator(
            tieBusinessNameObservable,
            tieTradeNameObservable,
            tieGovernmentIdObservable,
            tieMobileNumberObservable,
            tieBusinessAddressObservable,
            tieBarangayObservable,
            tieCityObservable,
            tieProvinceObservable,
            tieZipCodeObservable,
            houseNoObservable,
            streetObservable
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

    private fun showCountryDetails(countryCode: CountryCode?) {
        binding.viewCountryCode.tieCountryCode.setText(countryCode?.callingCode)
        binding.viewCountryCode.imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${countryCode?.code?.toLowerCase()}")
        )
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun navigateSingleSelector(selectorType: String) {
        val action =
            DaoCompanyInformationStepOneFragmentDirections.actionSelectorActivity(selectorType)
        findNavController().navigate(action)
    }

    private fun clearFormFocus() {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        binding.constraintLayout.requestFocus()
        binding.constraintLayout.isFocusableInTouchMode = true
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_company_information_step_1

    override val viewModelClassType: Class<DaoCompanyInformationStepOneViewModel>
        get() = DaoCompanyInformationStepOneViewModel::class.java
}
