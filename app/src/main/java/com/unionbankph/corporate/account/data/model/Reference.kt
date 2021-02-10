package com.unionbankph.corporate.account.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reference(
    @SerialName("reference_name")
    var referenceName: String? = null,
    @SerialName("reference_value")
    var referenceValue: String? = null
)