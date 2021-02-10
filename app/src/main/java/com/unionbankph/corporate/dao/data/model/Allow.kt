package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class Allow(
    @SerialName("view_transactions")
    var viewTransactions: Boolean? = null,
    @SerialName("create_transactions")
    var createTransactions: Boolean? = null,
    @SerialName("approve_transactions")
    var approveTransactions: Boolean? = null,
    @SerialName("system_administrator")
    var systemAdministrator: Boolean? = null
)