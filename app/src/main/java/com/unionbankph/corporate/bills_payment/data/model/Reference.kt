package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reference(

    @SerialName("index")
    var index: String? = null,

    @SerialName("type")
    var type: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("value")
    var value: String? = null
)
