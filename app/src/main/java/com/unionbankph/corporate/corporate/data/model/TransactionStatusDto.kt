package com.unionbankph.corporate.corporate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionStatusDto(
    @SerialName("type")
    var type: String? = null,
    @SerialName("description")
    var description: String? = null,
    @SerialName("remarks")
    var remarks: String? = null,
    @SerialName("contextual_class")
    var contextualClass: String? = null
)
