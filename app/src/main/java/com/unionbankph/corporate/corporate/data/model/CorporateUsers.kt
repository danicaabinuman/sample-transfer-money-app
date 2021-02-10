package com.unionbankph.corporate.corporate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CorporateUsers(
    @SerialName("role_id")
    val roleId: String? = null,
    @SerialName("role_name")
    val roleName: String? = null,
    @SerialName("organization_name")
    val organizationName: String? = null,
    @SerialName("organization_id")
    val organizationId: String? = null,

    var colored: Boolean = false,

    var badgeCount: Int = 0
)
