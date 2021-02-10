package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmailConfirmationForm(

    @SerialName("confirmation_token")
    var confirmationToken: String? = null
)
