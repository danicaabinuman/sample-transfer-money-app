package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResendOTPForm(

    @SerialName("request_id")
    var requestId: String? = null
)
