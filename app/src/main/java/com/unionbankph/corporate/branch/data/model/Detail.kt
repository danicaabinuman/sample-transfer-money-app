package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Detail(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("check_date")
    var checkDate: String? = null,

    @SerialName("check_number")
    var checkNumber: String? = null,

    @SerialName("credit_account_number")
    var creditAccountNumber: String? = null,

    @SerialName("credits")
    var credits: MutableList<Credits>? = null,

    @SerialName("debit_account_number")
    var debitAccountNumber: String? = null,

    @SerialName("debits")
    var debits: MutableList<Debits>? = null,

    @SerialName("payment_mode")
    var paymentMode: String? = null,

    @SerialName("purpose")
    var purpose: String? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("representative_name")
    var representativeName: RepresentativeName? = null
)
