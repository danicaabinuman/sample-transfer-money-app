package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordRecoveryForm(

    @SerialName("email_address")
    var emailAddress: String? = null
)
