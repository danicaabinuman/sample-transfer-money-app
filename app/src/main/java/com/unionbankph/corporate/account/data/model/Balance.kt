package com.unionbankph.corporate.account.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Balance(
    @SerialName("from")
    var from: String? = null,
    @SerialName("end")
    var end: String? = null,
    @SerialName("currency")
    var currency: String? = null,
    @SerialName("has_more_data")
    var hasMoreData: String? = null
)
