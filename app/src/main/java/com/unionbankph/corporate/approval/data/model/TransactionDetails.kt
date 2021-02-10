package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDetails(
    
    @SerialName("header")
    var header: String? = null,
    
    @SerialName("value")
    var value: String? = null,
    
    @SerialName("display_index")
    var displayIndex: Int? = null
)