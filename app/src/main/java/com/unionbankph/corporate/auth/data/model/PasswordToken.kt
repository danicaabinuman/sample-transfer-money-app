package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordToken(

    @SerialName("password_token")
    val passwordToken: String? = null
)
