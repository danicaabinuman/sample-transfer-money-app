package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Debits(

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("check_number")
    var checkNumber: String? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("sort_code")
    var sortCode: String? = null,

    @SerialName("check_date")
    var checkDate: String? = null,

    @SerialName("remarks")
    var remarks: String? = null
)
