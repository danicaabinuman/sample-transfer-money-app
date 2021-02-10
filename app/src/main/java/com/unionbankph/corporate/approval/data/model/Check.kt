package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Check(
    @SerialName("payee_name")
    var payeeName: String? = null,
    @SerialName("amount")
    var amount: String? = null,
    @SerialName("currency")
    var currency: String? = null,
    @SerialName("branch")
    var branch: String? = null
)