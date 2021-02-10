package com.unionbankph.corporate.branch.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Detail(

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("debits")
    var debits: MutableList<Debits>? = null,

    @SerialName("credits")
    var credits: MutableList<Credits>? = null
)
