package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ECredOTPForm(

    @SerialName("requestId")
    var requestId: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("type")
    var type: String? = null
)
