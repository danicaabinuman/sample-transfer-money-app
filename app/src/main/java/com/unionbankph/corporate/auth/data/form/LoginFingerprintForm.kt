package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginFingerprintForm(

    @SerialName("udid")
    var udid: String? = null,

    @SerialName("finger_print_token")
    var fingerprintToken: String? = null,

    @SerialName("registration_token")
    var registrationToken: String? = null,

    @SerialName("username")
    var username: String? = null
)
