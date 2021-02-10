package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Details(

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("references")
    var references: MutableList<Reference> = mutableListOf(),

    @SerialName("message")
    var message: String? = null,

    @SerialName("reply_code")
    var replyCode: String? = null
)
