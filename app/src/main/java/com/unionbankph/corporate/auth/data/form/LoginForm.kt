package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginForm(

    @SerialName("username")
    var username: String? = null,

    @SerialName("password")
    var password: String? = null,

    @SerialName("udid")
    var udid: String? = null,

    @SerialName("registration_token")
    var registrationToken: String? = null
)
