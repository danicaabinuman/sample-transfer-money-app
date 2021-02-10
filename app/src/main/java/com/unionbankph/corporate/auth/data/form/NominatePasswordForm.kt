package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatePasswordForm(

    @SerialName("password")
    var password: String? = null,

    @SerialName("password_token")
    var passwordToken: String? = null
)
