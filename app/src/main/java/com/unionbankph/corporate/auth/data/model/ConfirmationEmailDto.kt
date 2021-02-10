package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationEmailDto(

    @SerialName("message")
    val message: String? = null,

    @SerialName("description")
    val description: String? = null
)
