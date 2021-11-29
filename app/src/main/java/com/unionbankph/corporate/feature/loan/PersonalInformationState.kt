package com.unionbankph.corporate.feature.loan

import androidx.annotation.IntDef
import androidx.annotation.StringDef


@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    PersonalInformationField.FIRST_NAME,
    PersonalInformationField.MIDDLE_NAME,
    PersonalInformationField.LAST_NAME,
    PersonalInformationField.DOB,
    PersonalInformationField.POB,
    PersonalInformationField.GENDER,
    PersonalInformationField.CIVIL_STATUS,
    PersonalInformationField.DEPENDENTS,
    PersonalInformationField.MOBILE,
    PersonalInformationField.EMAIL
)
annotation class PersonalInformationField {
    companion object {
        const val FIRST_NAME = 0
        const val MIDDLE_NAME = 1
        const val LAST_NAME = 2
        const val DOB = 3
        const val POB = 4
        const val GENDER = 5
        const val CIVIL_STATUS = 6
        const val DEPENDENTS = 7
        const val MOBILE = 8
        const val EMAIL = 9
    }
}

@Retention(AnnotationRetention.RUNTIME)
@StringDef(
    GenderType.FEMALE,
    GenderType.MALE
)
annotation class GenderType {
    companion object {
        const val FEMALE = "male"
        const val MALE = "female"
    }
}


data class PersonalInformationForm(
    var firstname: String? = null,
    var middlename: String? = null,
    var lastname: String? = null,
    var dob: String? = null,
    var pob: String? = null,
    var gender: String? = null,
    var civilStatus: String? = null,
    var dependents: String? = null,
    var mobile: String? = null,
    var email: String? = null,
    var formField: Int? = null,
)

data class PersonalInformationState(
    var firstnameError: String? = null,
    var middlenameError: String? = null,
    var lastnameError: String? = null,
    var dobError: String? = null,
    var pobError: String? = null,
    var genderError: String? = null,
    var civilStatusError: String? = null,
    var dependentsError: String? = null,
    var mobileError: String? = null,
    var emailError: String? = null,
    var isDataValid: Boolean = false
)