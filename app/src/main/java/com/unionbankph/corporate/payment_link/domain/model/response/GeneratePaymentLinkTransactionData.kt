package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneratePaymentLinkTransactionData(

    @SerialName("transactionId")
    var transactionId: String? = null,

    @SerialName("createdDate")
    var createdDate: String? = null




)