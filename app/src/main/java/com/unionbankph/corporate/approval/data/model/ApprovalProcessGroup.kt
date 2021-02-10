package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalProcessGroup(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("status")
    var status: String? = null,
    
    @SerialName("approval_group")
    var approvalGroup: ApprovalGroup? = null,
    
    @SerialName("approval_process_organization_users")
    var approvalProcessOrganizationUsers: MutableList<ApprovalProcessOrganizationUser>? = null
)