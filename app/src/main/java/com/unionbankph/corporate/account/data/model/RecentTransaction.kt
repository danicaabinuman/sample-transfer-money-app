package com.unionbankph.corporate.account.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentTransaction(

    @SerialName("balance")
    var balance: Balance? = null,

    @SerialName("records")
    var records: MutableList<Record> = mutableListOf(),

    @SerialName("limit")
    var limit: String? = null,

    @SerialName("page")
    var page: String? = null,

    @SerialName("total_records")
    var totalRecords: String? = null,

    @SerialName("last_running_balance")
    var lastRunningBalance: String? = null,

    @SerialName("next_page")
    var nextPage: String? = null
)
