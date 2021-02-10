package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ECredLoginOTPDto(

    @SerialName("accessToken")
    var accessToken: String? = null,

    @SerialName("fullName")
    var fullName: String? = null,

    @SerialName("emailAddress")
    var emailAddress: String? = null,

    @SerialName("mobileNumber")
    var mobileNumber: String? = null
)
