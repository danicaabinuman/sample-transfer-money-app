package com.unionbankph.corporate.loan.personal_information

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.isValidEmail
import com.unionbankph.corporate.app.common.extension.isValidPhone
import com.unionbankph.corporate.app.common.extension.updateFields
import com.unionbankph.corporate.feature.loan.PersonalInformationField
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.CIVIL_STATUS
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.DEPENDENTS
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.DOB
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.EMAIL
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.FIRST_NAME
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.GENDER
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.LAST_NAME
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.MIDDLE_NAME
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.MOBILE
import com.unionbankph.corporate.feature.loan.PersonalInformationField.Companion.POB
import com.unionbankph.corporate.feature.loan.PersonalInformationForm
import com.unionbankph.corporate.feature.loan.PersonalInformationState
import com.unionbankph.corporate.user_creation.presentation.enter_contact_info.UcEnterContactInfoFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.absoluteValue


class PersonalInformationViewModel @Inject
constructor(
    val context: Context
) : BaseViewModel() {

    private val _formState = MutableLiveData(PersonalInformationState())
    val formState: LiveData<PersonalInformationState> = _formState

    private val _form = MutableLiveData<PersonalInformationForm>()
    val form: LiveData<PersonalInformationForm> = _form

    private val _personalInformationSuccess = MutableLiveData(false)
    val personalInformationSuccess: LiveData<Boolean> = _personalInformationSuccess

    private val _te = MutableLiveData<Int>()
    val te: LiveData<Int> = _te

    val placeOfBirth = MutableLiveData<List<String>>()

    private val dataStreamFlow = MutableSharedFlow<Pair<String, Int>>()

    @SuppressLint("ResourceType")
    private val formObserver = Observer<PersonalInformationForm> { form ->
        if (form != null) {
            _formState.updateFields {
                it.value?.let { state ->
                    state.apply {
                        when (form.formField) {
                            FIRST_NAME -> {
                                firstnameError = if (form.firstname.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_first_name))
                                } else {
                                    null
                                }
                            }
                            MIDDLE_NAME -> {
                                middlenameError = if (form.middlename.isNullOrBlank()) {
                                    context.getString(R.string.error_specific_field, context.getString(R.string.title_middle_name))
                                } else {
                                    null
                                }
                            }
                            LAST_NAME -> {
                                lastnameError =
                                    if (form.lastname.isNullOrBlank()) {
                                        context.getString(R.string.error_specific_field, context.getString(R.string.hint_last_name))
                                    } else {
                                        null
                                    }
                            }
                            DOB -> {
                                dobError =
                                    if (form.dob.isNullOrBlank() /* == context.resources.getString(R.string.hint_date_of_birth)*/) {
//                                        context.getString(R.string.error_invalid_input)
                                        "Age must be 18 years old or above"
                                    } else {
                                        null
                                    }
                            }
                            POB -> {
                                pobError =
                                    if (form.pob.isNullOrBlank()) {
                                        context.getString(R.string.error_invalid_input)
                                    } else {
                                        null
                                    }
                            }
                            GENDER -> {
                                genderError =
                                    if (form.gender.isNullOrBlank()) {
                                        context.getString(R.string.error_invalid_input)
                                    } else {
                                        null
                                    }
                            }
                            CIVIL_STATUS -> {
                                civilStatusError =
                                    if (form.civilStatus.isNullOrBlank()) {
                                        context.getString(R.string.error_invalid_input)
                                    } else {
                                        null
                                    }
                            }
                            DEPENDENTS -> {
                                dependentsError =
                                    if (form.dependents.isNullOrBlank()) {
                                        context.getString(R.string.error_specific_field, context.getString(R.string.hint_number_of_dependents))
                                    } else {
                                        null
                                    }
                            }
                            MOBILE -> {
                                mobileError =
                                    if (form.mobile.isNullOrBlank()) {
                                        context.getString(R.string.error_specific_field, context.getString(R.string.title_mobile_number))
                                    } else if (!form.mobile.isValidPhone()) {
                                        context.getString(R.string.error_mobile_field)
                                    } else if (form.mobile.toString().length < 10) {
                                        context.getString(R.string.error_validation_min, context.getString(R.integer.max_length_mobile_number_ph))
                                    } else {
                                        null
                                    }
                            }
                            EMAIL -> {
                                emailError =
                                    if (!form.email.isValidEmail()) {
                                        context.getString(R.string.error_specific_field, context.getString(R.string.title_email))
                                    } else {
                                        null
                                    }
                            }
                        }
                        form.apply {
                            isDataValid =
                                form.firstname?.isNotEmpty() == true && /*form.middlename?.isNotEmpty() == true &&*/
                                        form.lastname?.isNotEmpty() == true && form.dob?.isNotEmpty() == true &&
                                         pob?.isNotEmpty() == true && civilStatus?.isNotEmpty() == true &&
                                        form.gender?.isNotEmpty() == true && form.dependents?.isNotEmpty() == true &&
                                        form.mobile?.isValidPhone() == true && form.email?.isValidEmail() == true
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

        getPlaceOfBirth()
    }

    private fun getPlaceOfBirth() {

        placeOfBirth.value = context.resources.getStringArray(R.array.dummy_place_of_birth).toList()
    }

    private suspend fun observeDataStream() {
        dataStreamFlow.debounce(100)
            .collect { data ->
                _form.updateFields {
                    if (it.value == null) {
                        it.value = PersonalInformationForm()
                    }
                    it.value?.let { form ->
                        when (data.second) {
                            FIRST_NAME -> form.firstname = data.first
                            MIDDLE_NAME -> form.middlename = data.first
                            LAST_NAME -> form.lastname = data.first
                            DOB -> {
                                if (data.first == context.resources.getString(R.string.hint_date_of_birth)) {
                                    form.dob = null //context.resources.getString(R.string.hint_date_of_birth)
                                } else {
                                    form.dob = data.first
                                }
                            }
                            POB -> form.pob = data.first
                            GENDER -> form.gender = data.first
                            CIVIL_STATUS -> form.civilStatus = data.first
                            DEPENDENTS -> form.dependents = data.first
                            MOBILE -> form.mobile = data.first
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