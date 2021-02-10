package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Client(

    @SerialName("branch_sol_id")
    var branchSolId: String? = null,

    @SerialName("corporation_id")
    var corporationId: String? = null,

    @SerialName("email")
    var email: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("phone_number")
    var phoneNumber: String? = null,

    @SerialName("branch_name")
    var branchName: String? = null,

    @SerialName("branch_address")
    var branchAddress: String? = null,

    @SerialName("priority_class")
    var priorityClass: Int? = null,

    @SerialName("transaction_date")
    var transactionDate: String? = null,

    @SerialName("remarks")
    var remarks: String? = null
)
