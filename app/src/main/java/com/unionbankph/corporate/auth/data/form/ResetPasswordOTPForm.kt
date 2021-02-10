package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordOTPForm(

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("code")
    var code: String? = null
)
