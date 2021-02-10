package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Auth(

    @SerialName("type")
    val type: String? = null,

    @SerialName("validity")
    val validity: String? = null,

    @SerialName("request_id")
    var requestId: String? = null,

    @SerialName("mobile_number")
    val mobileNumber: String? = null,

    @SerialName("invalid_attempts")
    val invalidAttempts: Int? = null
)
