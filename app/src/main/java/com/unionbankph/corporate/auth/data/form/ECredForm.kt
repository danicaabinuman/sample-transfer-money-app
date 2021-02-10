package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ECredForm(

    @SerialName("emailAddress")
    var emailAddress: String? = null,

    @SerialName("password")
    var password: String? = null,

    @SerialName("mobileNumber")
    var mobileNumber: String? = null,

    @SerialName("countryCodeId")
    var countryCodeId: Int? = null
)
