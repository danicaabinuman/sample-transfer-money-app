package com.unionbankph.corporate.account.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillsPayment(
    @SerialName("biller_name")
    var billerName: String? = null,
    @SerialName("channel")
    var channel: String? = null,
    @SerialName("references")
    var references: MutableList<Reference>? = null,
    @SerialName("reversal_date")
    var reversalDate: String? = null,
    @SerialName("reversal_flag")
    var reversalFlag: String? = null,
    @SerialName("reversal_reference_number")
    var reversalReferenceNumber: String? = null
)