package com.unionbankph.corporate.branch.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchTransaction(

    @SerialName("position")
    var position: Int = 0,

    @SerialName("branch_transaction_form")
    var branchTransactionForm: BranchTransactionForm? = null
)
