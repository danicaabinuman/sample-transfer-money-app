package com.unionbankph.corporate.dao.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationStepOneForm(
    @SerialName("country_code")
    var countryCode: Int? = null,
    @SerialName("mobile_number")
    var mobileNumber: String? = null,
    @SerialName("gender")
    var gender: Int? = null,
    @SerialName("civil_status")
    var civilStatus: Int? = null,
    @SerialName("page")
    var page: Int = 1
)
