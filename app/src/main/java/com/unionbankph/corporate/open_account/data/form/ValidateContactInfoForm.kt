package com.unionbankph.corporate.open_account.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateContactInfoForm(

    @SerialName("first_name")
    var firstName: String? = null,
    @SerialName("last_name")
    var lastName: String? = null,
    @SerialName("email_address")
    var email_address: String? = null,
    @SerialName("mobile_number")
    var mobile_number: String? = null,
    @SerialName("country_code_id")
    var country_code_id: Int? = null
)