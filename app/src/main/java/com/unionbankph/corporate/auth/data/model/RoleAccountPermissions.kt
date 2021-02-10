package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoleAccountPermissions(
    @SerialName("account_id")
    val accountId: Int? = null,
    @SerialName("product_permissions")
    val productPermissions: MutableList<ProductPermissions>? = null
)
