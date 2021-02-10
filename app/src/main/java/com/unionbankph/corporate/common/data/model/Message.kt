package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(

    @SerialName("message")
    val message: String? = null,

    @SerialName("description")
    val description: String? = null
)
