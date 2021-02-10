package com.unionbankph.corporate.corporate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Permission(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("product_id")
    var productId: Int? = null,

    @SerialName("parent_id")
    var parentId: Int? = null,

    @SerialName("category_name")
    var categoryName: String? = null
)
