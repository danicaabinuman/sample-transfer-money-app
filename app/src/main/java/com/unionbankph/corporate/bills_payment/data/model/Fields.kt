package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fields(

    @SerialName("index")
    val index: Int? = null,

    @SerialName("value")
    val value: String? = null,

    @SerialName("name")
    val name: String? = null
)
