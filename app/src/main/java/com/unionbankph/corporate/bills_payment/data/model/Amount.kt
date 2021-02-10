package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Amount(

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("value")
    var value: Double? = null
)
