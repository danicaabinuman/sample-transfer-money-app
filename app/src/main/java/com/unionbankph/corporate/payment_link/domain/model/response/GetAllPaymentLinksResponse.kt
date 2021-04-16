package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAllPaymentLinksResponse(

    @SerialName("referenceNo")
    var referenceNo: String? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("expiryDate")
    var expiryDate: String? = null,

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("note")
    var description: String? = null

)