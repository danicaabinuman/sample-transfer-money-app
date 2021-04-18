package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutPaymentLinkStatusDetailsModel(

    @SerialName("status")
    var status: String? = null,

    @SerialName("fee")
    var fee: Int? = null,

    @SerialName("paymentMethod")
    var paymentMethod: String? = null

)