package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominateEmailDto(

    @SerialName("status")
    val status: String? = null
)
