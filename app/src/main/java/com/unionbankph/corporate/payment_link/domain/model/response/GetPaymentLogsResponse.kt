package com.unionbankph.corporate.payment_link.domain.model.response

import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class GetPaymentLogsResponse(

    @SerialName("records")
    var records: List<PaymentLogsModel>? = null
)
