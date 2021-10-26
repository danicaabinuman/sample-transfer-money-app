package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdDto(
    @SerialName("id")
    var id: String? = null,
    @SerialName("idName")
    var name: String? = null
)
