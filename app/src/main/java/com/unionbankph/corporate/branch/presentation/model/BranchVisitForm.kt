package com.unionbankph.corporate.branch.presentation.model

import com.unionbankph.corporate.branch.data.model.Client
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchVisitForm(

    @SerialName("channel")
    var channel: String? = null,

    @SerialName("client")
    var client: Client? = null,

    @SerialName("transactions")
    var transactions: MutableList<Transaction>? = null
)
