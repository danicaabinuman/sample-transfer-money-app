package com.unionbankph.corporate.loan.financial_information

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.updateFields
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.GSIS_ID
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.MONTHLY_INCOME
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.OCCUPATION
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.POSITION
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.SSS_ID
import com.unionbankph.corporate.feature.loan.FinancialInformationField.Companion.TIN_ID
import com.unionbankph.corporate.feature.loan.FinancialInformationForm
import com.unionbankph.corporate.feature.loan.FinancialInformationState
import com.unionbankph.corporate.feature.loan.PersonalInformationField
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject


@FlowPreview
@SuppressLint("StaticFieldLeak")
class FinancialInformationViewModel @Inject
constructor(
    val context: Context
) : BaseViewModel() {

    private val _formState = MutableLiveData(FinancialInformationState())
    val formState: LiveData<FinancialInformationState> = _formState

    private val _form = MutableLiveData<FinancialInformationForm>()
    val form: LiveData<FinancialInformationForm> = _form

    private val _monthlyIncome = MutableLiveData<String>()
    val monthlyIncome: LiveData<String> = _monthlyIncome

    private val _financialInformationSuccess = MutableLiveData(false)
    val financialInformationSuccess: LiveData<Boolean> = _financialInformationSuccess

    private val dataStreamFlow = MutableSharedFlow<Pair<String, Int>>()

    private val formObserver = Observer<FinancialInformationForm> { form ->
        if (form != null) {
            _formState.updateFields {
                it.value?.let { state ->
                    state.apply {
                        when (form.formField) {
                            OCCUPATION -> {
                                occupationError = if (form.occupation.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_occupation))
                                } else {
                                    null
                                }
                            }
                            POSITION -> {
                                positionError = if (form.position.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_position))
                                } else {
                                    null
                                }
                            }
                            MONTHLY_INCOME -> {
                                monthlyIncomeError = if (form.monthlyIncome?.replace("PHP ", "").isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_monthly_income))
                                } else {
                                    null
                                }
                            }
                            TIN_ID -> {
                                tinIdError = if (form.tinId?.length!! < 9) {
                                    "Minimum of 9 characters"
                                } else if (form.tinId.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_tin_id))
                                } else {
                                    null
                                }
                            }
                            SSS_ID -> {
                                sssIdError = if (form.sssId?.length!! < 10) {
                                    "Minimum of 10 characters"
                                } else if (form.sssId.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_sss_id_optional))
                                } else {
                                    null
                                }
                            }
                            GSIS_ID -> {
                                gsisIdError = if (form.gsisId?.length!! < 11) {
                                    "Minimum of 11 characters"
                                } else if (form.gsisId.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_gsis_id_optional))
                                } else {
                                    null
                                }
                            }
                        }

                        form.apply {
                        isDataValid = occupation?.isNotEmpty() == true && position?.isNotEmpty() == true &&
                                !monthlyIncome?.replace("PHP ", "").isNullOrBlank() &&
                                tinId?.isNotEmpty() == true && tinId?.length!! >= 9
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
        dataStreamFlow.debounce(0)
            .collect { data ->
                _form.updateFields {
                    if (it.value == null) {
                        it.value = FinancialInformationForm()
                    }
                    it.value?.let { form ->
                        when (data.second) {
                            OCCUPATION -> form.occupation = data.first
                            POSITION -> form.position = data.first
                            MONTHLY_INCOME -> {
                                form.monthlyIncome = data.first
                                //val newValue = data.first.replace("PHP ","").replace(",","")
                                ///_monthlyIncome.value = context.getString(R.string.format_amount_comma, newValue.toFloat())
                            }
                            TIN_ID -> form.tinId = data.first
                            SSS_ID -> form.sssId = data.first
                            GSIS_ID -> form.gsisId = data.first
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