package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CWTDetail(

    @SerialName("order")
    var order: Long? = null,

    @SerialName("display")
    var display: String? = null,

    @SerialName("key")
    var key: String? = null
)
