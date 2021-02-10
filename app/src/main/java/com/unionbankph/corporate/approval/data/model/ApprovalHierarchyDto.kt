package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalHierarchyDto(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("channel")
    var channel: String? = null,
    
    @SerialName("status")
    var status: String? = null,
    
    @SerialName("approval_hierarchy")
    var approvalHierarchy: ApprovalHierarchy? = null,
    
    @SerialName("batch_id")
    var batchId: String? = null,
    
    @SerialName("approval_process_organization_users")
    var approvalProcessOrganizationUsers: MutableList<ApprovalProcessOrganizationUser>? = null,
    
    @SerialName("approval_process_groups")
    var approvalProcessGroups: MutableList<ApprovalProcessGroup>? = null,
    
    @SerialName("approval_process_nodes")
    var approvalProcessNodes: MutableList<ApprovalProcessNode>? = null
)