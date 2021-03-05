package com.unionbankph.corporate.request_payment_link.data.model

import com.unionbankph.corporate.bills_payment.data.model.Transaction
import kotlinx.serialization.SerialName

data class RequestPaymentLinkResponse(
        @SerialName("datetime")
        var datetime: String? = null,

        @SerialName("status")
        var status: Int? = null,

        @SerialName("message")
        val message: String? = null,

        @SerialName("path")
        var path: String? = null

)
