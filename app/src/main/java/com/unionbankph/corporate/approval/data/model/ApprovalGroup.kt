package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ApprovalGroup(
    
    @SerialName("id")
    var id: Int? = null,
    
    @SerialName("group_name")
    var groupName: String? = null,
    
    @SerialName("organization_users")
    var organizationUsers: MutableList<OrganizationUser> = mutableListOf(),
    
    @SerialName("schema_id")
    var schemaId: String? = null
)