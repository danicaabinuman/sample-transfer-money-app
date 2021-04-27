package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneratePaymentLinkTransactionData(

    @SerialName("transactionId")
    var transactionId: String? = null,

    @SerialName("dateTime")
    var dateTime: String? = null,

    @SerialName("transactionDate")
    var transactionDate: String? = null,

    @SerialName("createdDate")
    var createdDate: String? = null,

    @SerialName("modifiedDate")
    var modifiedDate: String? = null




)