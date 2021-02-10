package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalProcessOrganizationUser(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("status")
    var status: String? = null,
    
    @SerialName("organization_user")
    var organizationUser: OrganizationUser? = null
)