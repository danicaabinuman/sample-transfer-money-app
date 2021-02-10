package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalProcessNode(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("status")
    var status: String? = null,
    
    @SerialName("approval_hierarchy_node")
    var approvalHierarchyNode: ApprovalHierarchyNode? = null
)