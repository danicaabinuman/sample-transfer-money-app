package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ECredEmailConfirmationForm(

    @SerialName("token")
    var token: String? = null
)
