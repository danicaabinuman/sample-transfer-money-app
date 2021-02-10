package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Error(
    @SerialName("message")
    var message: String? = null,
    @SerialName("description")
    var description: String? = null,
    @SerialName("code")
    var code: String? = null,
    @SerialName("heading")
    var heading: String? = null,
    @SerialName("details")
    var details: Details? = null
)
