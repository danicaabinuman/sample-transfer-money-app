package com.unionbankph.corporate.request_payment_link.data.model

import com.unionbankph.corporate.bills_payment.data.model.Transaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestPaymentLinkResponse(
        @SerialName("id")
        var id: String? = null,

        @SerialName("referenceId")
        var referenceId: Int? = null,

        @SerialName("amount")
        val amount: Double? = null,

        @SerialName("description")
        var description: String? = null,

        @SerialName("note")
        var note: String? = null,

        @SerialName("expireDate")
        var expireDate: String? = null,

        @SerialName("status")
        var status: String? = null,

        @SerialName("link")
        var link: String? = null,

        @SerialName("createdBy")
        var createdBy: String? = null,

        @SerialName("createdDate")
        var createdDate: String? = null,

        @SerialName("modifiedBy")
        var modifiedBy: String? = null,

        @SerialName("modifiedDate")
        var modifiedDate: String? = null
)
