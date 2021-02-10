package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchVisitDto(

    @SerialName("ambassador_name")
    var ambassadorName: String? = null,

    @SerialName("branch_transaction_reference_number")
    var branchTransactionReferenceNumber: String? = null,

    @SerialName("channel_id")
    var channelId: String? = null,

    @SerialName("client")
    var client: Client? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("modified_by")
    var modifiedBy: String? = null,

    @SerialName("modified_date")
    var modifiedDate: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("reference_id")
    var referenceId: String? = null,

    @SerialName("approval_process_id")
    var approvalProcessId: String? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("teller_name")
    var tellerName: String? = null,

    @SerialName("transactions")
    var transactions: MutableList<Transaction>? = null
)
