package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillsPaymentValidate(

    @SerialName("message")
    var message: String? = null,

    @SerialName("has_similar_payment")
    var hasSimilarPayment: Boolean? = null,

    @SerialName("details")
    var details: Details? = null
)
