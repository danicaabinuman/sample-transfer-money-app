package com.unionbankph.corporate.open_account.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAccountForm(
    @SerialName("first_name")
    var firstName: String? = null,
    @SerialName("last_name")
    var lastName: String? = null,
    @SerialName("email")
    var email: String? = null,
    @SerialName("countryCode")
    var countryCode: String? = null,
    @SerialName("mobile")
    var mobile: String? = null
)
