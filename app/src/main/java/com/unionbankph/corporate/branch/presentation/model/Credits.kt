package com.unionbankph.corporate.branch.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Credits(

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null
)
