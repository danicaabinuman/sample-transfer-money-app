package com.unionbankph.corporate.payment_link.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SMEApiError(
    @SerialName("message")
    var message: String? = null,
    @SerialName("httpStatus")
    var httpStatus: String? = null,
    @SerialName("timestamp")
    var timestamp: String? = null
)