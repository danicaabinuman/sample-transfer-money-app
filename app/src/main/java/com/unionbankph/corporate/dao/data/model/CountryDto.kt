package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    @SerialName("id")
    var id: String? = null,
    @SerialName("name")
    var name: String? = null
)
