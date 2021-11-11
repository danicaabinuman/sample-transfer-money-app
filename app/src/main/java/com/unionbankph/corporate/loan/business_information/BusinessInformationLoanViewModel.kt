package com.unionbankph.corporate.loan.business_information

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.isValidPhone
import com.unionbankph.corporate.app.common.extension.updateFields
import com.unionbankph.corporate.feature.loan.*
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.BUSINESS_TIN
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.DATE_STARTED_BUSINESS
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.EMAIL
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.INDUSTRY
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.LANDLINE
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.LEGAL_NAME
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.MOBILE
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.NUMBER_OF_EMPLOYEE
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.ORGANIZATION
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.GSIS_ID
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.MONTHLY_INCOME
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.OCCUPATION
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.POSITION
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.SSS_ID
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.TIN_ID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject


class BusinessInformationLoanViewModel @Inject
constructor(
    val context: Context
) : BaseViewModel() {

    private val _formState = MutableLiveData(BusinessInformationState())
    val formState: LiveData<BusinessInformationState> = _formState

    private val _form = MutableLiveData<BusinessInformationForm>()
    val form: LiveData<BusinessInformationForm> = _form

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    private val dataStreamFlow = MutableSharedFlow<Pair<String, Int>>()

    private val formObserver = Observer<BusinessInformationForm> { form ->
        if (form != null) {
            _formState.updateFields {
                it.value?.let { state ->
                    state.apply {
                        when (form.formField) {
                            LEGAL_NAME -> {
                                legalnameError = if (form.legalname.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.hint_full_legal_name))
                                } else {
                                    null
                                }
                            }
                            INDUSTRY -> {
                                industryError = if (form.industry.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_number_of_employees))
                                } else {
                                    null
                                }
                            }
                            ORGANIZATION -> {
                                organizationError = if (form.organization.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            NUMBER_OF_EMPLOYEE -> {
                                numberOfEmployeeError = if (form.numberOfEmployee.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_number_of_employees))
                                } else {
                                    null
                                }
                            }
                            BUSINESS_TIN -> {
                                businessTinError = if (form.businessTin.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.hint_business_tin))
                                } else {
                                    null
                                }
                            }
                            DATE_STARTED_BUSINESS -> {
                                dateStartedBusinessError = if (form.dateStartedBusiness.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            MOBILE -> {
                                mobileError = if (form.mobile.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_mobile_number))
                                } else if (!form.mobile.isValidPhone()) {
                                    context.getString(R.string.error_mobile_field)
                                } else if (form.mobile.toString().length < 10) {
                                    context.getString(R.string.error_validation_min, context.getString(R.integer.max_length_mobile_number_ph))
                                } else {
                                    null
                                }
                            }
                            LANDLINE -> {
                                landlineError = if (form.landline.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_landline))
                                } else {
                                    null
                                }
                            }
                            EMAIL -> {
                                emailError = if (form.email.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_email))
                                } else {
                                    null
                                }
                            }
                        }

                        form.apply {
                        isDataValid = legalname?.isNotEmpty() == true && industry?.isNotEmpty() == true &&
                                organization?.isNotEmpty() == true && numberOfEmployee?.isNotEmpty() == true &&
                                businessTin?.isNotEmpty() == true && dateStartedBusiness?.isNotEmpty() == true &&
                                mobile?.isNotEmpty() == true && /*landline?.isNotEmpty() == true &&*/ email?.isNotEmpty() == true
                        }
                    }
                }
            }
        }
    }

    init {

        viewModelScope.launch {
            observeDataStream()
        }
        form.observeForever(formObserver)
    }

    private suspend fun observeDataStream() {
        dataStreamFlow.debounce(500)
            .collect { data ->
                _form.updateFields {
                    if (it.value == null) {
                        it.value = BusinessInformationForm()
                    }
                    it.value?.let { form ->
                        when (data.second) {
                            LEGAL_NAME -> form.legalname = data.first
                            INDUSTRY -> form.industry = data.first
                            ORGANIZATION -> form.organization = data.first
                            NUMBER_OF_EMPLOYEE -> form.numberOfEmployee = data.first
                            BUSINESS_TIN -> form.businessTin = data.first
                            DATE_STARTED_BUSINESS -> form.dateStartedBusiness = data.first
                            MOBILE -> form.mobile = data.first
                            LANDLINE -> form.landline = data.first
                            EMAIL -> form.email = data.first
                        }
                        form.formField = data.second
                    }
                }
            }
    }

    fun onDataChange(text: CharSequence, field: Int) {
        viewModelScope.launch {
            dataStreamFlow.emit(Pair(text.toString(), field))
        }
    }

}