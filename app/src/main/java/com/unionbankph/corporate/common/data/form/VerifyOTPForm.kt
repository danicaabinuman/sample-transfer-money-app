package com.unionbankph.corporate.common.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOTPForm(

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("code")
    var code: String? = null
)
