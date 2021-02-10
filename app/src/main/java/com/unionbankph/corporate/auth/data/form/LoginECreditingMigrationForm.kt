package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginECreditingMigrationForm(

    @SerialName("username")
    val username: String? = null,

    @SerialName("password")
    val password: String? = null
)
