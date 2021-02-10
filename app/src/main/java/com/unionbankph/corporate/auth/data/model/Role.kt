package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(

    @SerialName("name")
    var name: String? = null,

    @SerialName("role_id")
    var roleId: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("organization_name")
    var organizationName: String? = null,

    @SerialName("role_account_permissions")
    var roleAccountPermissions: MutableList<RoleAccountPermissions>? = null,

    @SerialName("approval_groups")
    var approvalGroups: MutableList<Int>? = null,

    @SerialName("has_approval")
    var hasApproval: Boolean = true,

    @SerialName("is_approver")
    var isApprover: Boolean = true
)
