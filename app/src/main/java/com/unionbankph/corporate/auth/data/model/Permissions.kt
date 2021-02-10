package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Permissions(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("code")
    val code: String? = null,
    @SerialName("product_id")
    val productId: Int? = null,
    @SerialName("parent_id")
    val parentId: Int? = null,
    @SerialName("category_name")
    val categoryName: String? = null
)
