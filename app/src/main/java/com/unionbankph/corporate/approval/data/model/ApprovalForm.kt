package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalForm(
    
    @SerialName("approval_type")
    val approvalType: String? = null,
    
    @SerialName("reason_for_rejection")
    val reasonForRejection: String? = null,
    
    @SerialName("batch_ids")
    val batchIds: MutableList<String> = mutableListOf(),
    
    @SerialName("transaction_reference_numbers")
    val transactionReferenceNumbers: MutableList<String> = mutableListOf()
)