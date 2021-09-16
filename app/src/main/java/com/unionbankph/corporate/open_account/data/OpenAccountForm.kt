package com.unionbankph.corporate.open_account.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAccountForm(
    @SerialName("first_name")
    var firstName: String? = null,
    @SerialName("last_name")
    var lastName: String? = null,
    @SerialName("email_address")
    var emailAddress: String? = null,
    @SerialName("mobile_number")
    var mobileNumber: String? = null,
    @SerialName("country_code_id")
    var countryCodeId: String? = null

)
