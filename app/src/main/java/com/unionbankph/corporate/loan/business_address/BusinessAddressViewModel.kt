package com.unionbankph.corporate.loan.business_address

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.updateFields
import com.unionbankph.corporate.feature.loan.*
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.ADDRESS_LINE_ONE
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.ADDRESS_LINE_TWO
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.CITY
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.ESTABLISHMENT
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.PROVINCE
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.REGION
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.YEARS_IN_OPERATION
import com.unionbankph.corporate.feature.loan.BusinessAddressField.Companion.ZIP_CODE
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.BUSINESS_TIN
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.DATE_STARTED_BUSINESS
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.EMAIL
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.INDUSTRY
import com.unionbankph.corporate.feature.loan.BusinessInformationField.Companion.LANDLINE
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


class BusinessAddressViewModel @Inject
constructor(
    val context: Context
) : BaseViewModel() {

    private val _formState = MutableLiveData(BusinessAddressState())
    val formState: LiveData<BusinessAddressState> = _formState

    private val _form = MutableLiveData<BusinessAddressForm>()
    val form: LiveData<BusinessAddressForm> = _form

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    private val dataStreamFlow = MutableSharedFlow<Pair<String, Int>>()

    private val formObserver = Observer<BusinessAddressForm> { form ->
        if (form != null) {
            _formState.updateFields {
                it.value?.let { state ->
                    state.apply {
                        when (form.formField) {
                            ADDRESS_LINE_ONE -> {
                                addressLineOneError = if (form.addressLineOne.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_address))
                                } else {
                                    null
                                }
                            }
                            ADDRESS_LINE_TWO -> {
                                addressLineTwoError = if (form.addressLineTwo.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_address))
                                } else {
                                    null
                                }
                            }
                            CITY -> {
                                cityError = if (form.city.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            PROVINCE -> {
                                provinceError = if (form.province.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            REGION -> {
                                regionError = if (form.region.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            ZIP_CODE -> {
                                zipCodeError = if (form.zipCode.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.hint_zip_code))
                                } else {
                                    null
                                }
                            }
                            ESTABLISHMENT -> {
                                establishmentError = if (form.establishment.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            YEARS_IN_OPERATION -> {
                                yearsInOperationError = if (form.yearsInOperation.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_years_in_operation))
                                } else {
                                    null
                                }
                            }
                        }

                        form.apply {
                        isDataValid = addressLineOne?.isNotEmpty() == true && addressLineTwo?.isNotEmpty() == true &&
                                city?.isNotEmpty() == true && province?.isNotEmpty() == true &&
                                /*region?.isNotEmpty() == true &&*/ zipCode?.isNotEmpty() == true &&
                                establishment?.isNotEmpty() == true && yearsInOperation?.isNotEmpty() == true
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
                        it.value = BusinessAddressForm()
                    }
                    it.value?.let { form ->
                        when (data.second) {
                            ADDRESS_LINE_ONE -> form.addressLineOne = data.first
                            ADDRESS_LINE_TWO -> form.addressLineTwo = data.first
                            CITY -> form.city = data.first
                            PROVINCE -> form.province = data.first
                            REGION -> form.region = data.first
                            ZIP_CODE -> form.zipCode = data.first
                            ESTABLISHMENT -> form.establishment = data.first
                            YEARS_IN_OPERATION -> form.yearsInOperation = data.first
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