package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactValidityResponse(

    @SerialName("requestId")
    var requestId: String? = null,

    @SerialName("type")
    var type: String? = null,

    @SerialName("mobileNumber")
    var mobileNumber: String? = null,

    @SerialName("invalid_attempts")
    var invalidAttempts: Int? = null,

    @SerialName("validity")
    var validity: String? = null,

    @SerialName("otpType")
    var otpType: String? = null,

    @SerialName("mobileAvailable")
    var mobileAvailable: Boolean? = null,

    @SerialName("emailAvailable")
    var emailAvailable: Boolean? = null)
