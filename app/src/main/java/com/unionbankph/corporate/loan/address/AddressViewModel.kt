package com.unionbankph.corporate.loan.address

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.updateFields
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PERMANENT_ADDRESS_CITY
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PERMANENT_ADDRESS_LINE_ONE
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PERMANENT_ADDRESS_LINE_TWO
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PERMANENT_ADDRESS_PROVINCE
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PERMANENT_ADDRESS_ZIP_CODE
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PRESENT_ADDRESS_CITY
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PRESENT_ADDRESS_LINE_ONE
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PRESENT_ADDRESS_LINE_TWO
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PRESENT_ADDRESS_PROVINCE
import com.unionbankph.corporate.feature.loan.AddressInformationField.Companion.PRESENT_ADDRESS_ZIP_CODE
import com.unionbankph.corporate.feature.loan.AddressInformationForm
import com.unionbankph.corporate.feature.loan.AddressInformationState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject


class AddressViewModel @Inject constructor(
    val context: Context
) : BaseViewModel() {

    private val _formState = MutableLiveData(AddressInformationState())
    val formState: LiveData<AddressInformationState> = _formState

    private val _form = MutableLiveData<AddressInformationForm>()
    val form: LiveData<AddressInformationForm> = _form

    private val _sameAddress = MutableLiveData(true)
    val sameAddress: LiveData<Boolean> = _sameAddress

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    private val dataStreamFlow = MutableSharedFlow<Pair<String, Int>>()

    private val formObserver = Observer<AddressInformationForm> { form ->
        if (form != null) {
            _formState.updateFields {
                it.value?.let { state ->
                    state.apply {
                        when (form.formField) {
                            PRESENT_ADDRESS_LINE_ONE -> {
                                presentAddressLineOneError = if (form.presentAddressLineOne.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_address))
                                } else {
                                    null
                                }
                            }
                            PRESENT_ADDRESS_LINE_TWO -> {
                                presentAddressLineTwoError = if (form.presentAddressLineTwo.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_address))
                                } else {
                                    null
                                }
                            }
                            PRESENT_ADDRESS_PROVINCE -> {
                                presentAddressProvinceError = if (form.presentAddressProvince.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            PRESENT_ADDRESS_CITY -> {
                                presentAddressCityError = if (form.presentAddressCity.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            PRESENT_ADDRESS_ZIP_CODE -> {
                                presentAddressZipCodeError = if (form.presentAddressZipCode.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.hint_zip_code))
                                } else {
                                    null
                                }
                            }
                            PERMANENT_ADDRESS_LINE_ONE -> {
                                permanentAddressLineOneError = if (form.permanentAddressLineOne.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_address))
                                } else {
                                    null
                                }
                            }
                            PERMANENT_ADDRESS_LINE_TWO -> {
                                permanentAddressLineTwoError = if (form.permanentAddressLineTwo.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_address))
                                } else {
                                    null
                                }
                            }
                            PERMANENT_ADDRESS_PROVINCE -> {
                                permanentAddressProvinceError = if (form.permanentAddressProvince.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            PERMANENT_ADDRESS_CITY -> {
                                permanentAddressCityError = if (form.permanentAddressCity.isNullOrBlank()) {
                                    context.getString(R.string.error_invalid_input)
                                } else {
                                    null
                                }
                            }
                            PERMANENT_ADDRESS_ZIP_CODE -> {
                                permanentAddressZipCodeError = if (form.permanentAddressZipCode.isNullOrBlank()) {
                                    /*if (form.permanentAddressZipCode == "" && sameAddress.value == true && !form.presentAddressLineOne.isNullOrBlank() &&
                                        !form.presentAddressLineTwo.isNullOrBlank() && !form.presentAddressRegion.isNullOrBlank() &&
                                        !form.presentAddressCity.isNullOrBlank() && !form.presentAddressZipCode.isNullOrBlank()) {
                                        null
                                    } else {*/
                                        context.getString(R.string.error_specific_field, context.getString(R.string.hint_zip_code))
                                    /*}*/
                                } else {
                                    null
                                }
                            }
                        }

                        form.apply {
                            isDataValid = presentAddressLineOne?.isNotEmpty() == true /*&& presentAddressLineTwo?.isNotEmpty() == true*/ && presentAddressCity?.isNotEmpty() == true &&
                                    presentAddressProvince?.isNotEmpty() == true && presentAddressZipCode?.isNotEmpty() == true && permanentAddressLineOne?.isNotEmpty() == true &&
                                    /*permanentAddressLineTwo?.isNotEmpty() == true &&*/ permanentAddressCity?.isNotEmpty() == true && permanentAddressProvince?.isNotEmpty() == true &&
                                    permanentAddressZipCode?.isNotEmpty() == true
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

    fun setSameAddress() {

       if (form.value != null) {
           if (sameAddress.value == true) {
               form.value?.let {
               it.permanentAddressLineOne = it.presentAddressLineOne
               it.permanentAddressLineTwo = it.presentAddressLineTwo
               it.permanentAddressProvince = it.presentAddressProvince
               it.permanentAddressCity = it.presentAddressCity
               it.permanentAddressZipCode = it.presentAddressZipCode
               }
           } else {
               form.value?.apply {
                   permanentAddressLineOne = null
                   permanentAddressLineTwo = null
                   permanentAddressCity = null
                   permanentAddressProvince = null
                   permanentAddressZipCode = null
                   //formField = null
               }
           }
           _form.value = _form.value
       }
        _sameAddress.value = !_sameAddress.value!!
    }

    //TODO optimize/clean logic
    private suspend fun observeDataStream() {
        dataStreamFlow.debounce(500)
            .collect { data ->
                _form.updateFields {
                    if (it.value == null) {
                        it.value = AddressInformationForm()
                    }
                    it.value?.let { form ->
                        when (data.second) {
                            PRESENT_ADDRESS_LINE_ONE -> form.presentAddressLineOne = data.first
                            PRESENT_ADDRESS_LINE_TWO -> form.presentAddressLineTwo = data.first
                            PRESENT_ADDRESS_CITY -> form.presentAddressCity = data.first
                            PRESENT_ADDRESS_PROVINCE -> form.presentAddressProvince = data.first
                            PRESENT_ADDRESS_ZIP_CODE -> form.presentAddressZipCode = data.first
                            PERMANENT_ADDRESS_LINE_ONE -> form.permanentAddressLineOne = data.first
                            PERMANENT_ADDRESS_LINE_TWO -> form.permanentAddressLineTwo = data.first
                            PERMANENT_ADDRESS_CITY -> form.permanentAddressCity = data.first
                            PERMANENT_ADDRESS_PROVINCE -> form.permanentAddressProvince = data.first
                            PERMANENT_ADDRESS_ZIP_CODE -> form.permanentAddressZipCode = data.first
                        }
                        form.formField = data.second

//                        if (data.second == PERMANENT_ADDRESS_ZIP_CODE && sameAddress.value == true) {
//                            form.formField = null
//                        }

                        /*if (sameAddress.value == false && !form.presentAddressLineOne.isNullOrEmpty() &&
                            !form.presentAddressLineTwo.isNullOrEmpty() && !form.presentAddressCity.isNullOrEmpty() &&
                            !form.presentAddressRegion.isNullOrEmpty() && !form.presentAddressZipCode.isNullOrEmpty()) {
                            form.formField = null
                        } else {
                            if (sameAddress.value == true && !form.presentAddressLineOne.isNullOrEmpty() ||
                                !form.presentAddressLineTwo.isNullOrEmpty() || !form.presentAddressCity.isNullOrEmpty() ||
                                !form.presentAddressRegion.isNullOrEmpty() || !form.presentAddressZipCode.isNullOrEmpty()) {
                                form.permanentAddressZipCode = null
                            } else {
                                form.formField = data.second
                            }
                        }*/
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