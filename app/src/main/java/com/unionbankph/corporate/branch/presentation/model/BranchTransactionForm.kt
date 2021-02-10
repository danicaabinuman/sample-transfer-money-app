package com.unionbankph.corporate.branch.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchTransactionForm(

    @SerialName("type")
    var type: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("check_date")
    var checkDate: String? = null,

    @SerialName("check_number")
    var checkNumber: String? = null,

    @SerialName("remarks")
    var remarks: String? = null,
    var isSelected: Boolean = false
)
