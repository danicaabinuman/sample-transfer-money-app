package com.unionbankph.corporate.corporate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    var name: String? = null
)
