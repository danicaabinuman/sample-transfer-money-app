package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginOTPForm(

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("otp_type")
    var otpType: String? = null,

    @SerialName("registration_token")
    var registrationToken: String? = null
)
