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
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_company_information_step_1.*
import kotlinx.android.synthetic.main.widget_country_code.*
import java.util.concurrent.TimeUnit

class DaoCompanyInformationStepOneFragment :
    BaseFragment<DaoCompanyInformationStepOneViewModel>(R.layout.fragment_dao_company_information_step_1),
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
            )[DaoCompanyInformationStepOneViewModel::class.java]
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
        tie_business_name.refresh()
        tie_trade_name.refresh()
        tie_government_type.refresh()
        tie_government_id.refresh()
        tie_mobile_number.refresh()
        tie_business_address.refresh()
        tie_barangay.refresh()
        tie_city.refresh()
        tie_province.refresh()
        tie_zip_code.refresh()
        tie_house_no.refresh()
        tie_street.refresh()
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_company_information))
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(5)
    }

    private fun initBinding() {
        viewModel.governmentIdTypeInput
            .subscribe {
                tie_government_id.text?.clear()
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
        viewModel.cityInput
            .subscribe {
                tie_city.setText(it.value)
            }.addTo(disposables)
        viewModel.provinceInput
            .subscribe {
                tie_province.setText(it.value)
            }.addTo(disposables)
        viewModel.barangayInput
            .subscribe {
                tie_barangay.setText(it.value)
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
        tie_government_type.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.GOVERNMENT_ID.name)
        }
        tie_barangay.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.BARANGAY.name)
        }
        tie_city.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.CITY.name)
        }
        tie_province.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE.name)
        }
        tie_country_code.setOnClickListener {
            findNavController().navigate(R.id.action_country_activity)
        }
    }

    private fun validateForm() {
        val tieBusinessNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_business_name
        )
        val tieTradeNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_trade_name
        )
        val tieGovernmentIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_government_id
        )
        val tieMobileNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_mobile_number
        )
        val tieBusinessAddressObservable = RxValidator.createFor(tie_business_address)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    tie_business_address.hint
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
            editText = tie_barangay
        )
        val tieCityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_city
        )
        val tieProvinceObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_province
        )
        val tieZipCodeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_zip_code
        )
        val houseNoObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_house_no
        )
        val streetObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_street
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
        tie_country_code.setText(countryCode?.callingCode)
        imageViewFlag.setImageResource(
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
        constraint_layout.requestFocus()
        constraint_layout.isFocusableInTouchMode = true
    }
}
