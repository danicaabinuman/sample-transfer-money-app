package com.unionbankph.corporate.bills_payment.data.form

import com.unionbankph.corporate.bills_payment.data.model.Fields
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrequentBillerForm(

    @SerialName("name")
    var name: String? = null,

    @SerialName("biller_name")
    var billerName: String? = null,

    @SerialName("short_name")
    var shortName: String? = null,

    @SerialName("service_id")
    var serviceId: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("source_account_ids")
    var sourceAccountIds: MutableList<Int>? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("all_accounts_selected")
    var allAccountsSelected: Boolean? = null,

    @SerialName("fields")
    var fields: MutableList<Fields> = mutableListOf(),

    @SerialName("excluded_account_ids")
    var excludedAccountIds: MutableList<Int> = mutableListOf()
)
