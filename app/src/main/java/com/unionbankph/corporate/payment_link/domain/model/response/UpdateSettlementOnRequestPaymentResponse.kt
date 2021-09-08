package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateSettlementOnRequestPaymentResponse(

    @SerialName("message")
    var message: String? = null,

    @SerialName("httpStatus")
    var httpStatus: String? = null,

    @SerialName("timestamp")
    var timestamp: String? = null
)