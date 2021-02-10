package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityDto(
    @SerialName("id")
    var id: String? = null,
    @SerialName("value")
    var value: String? = null
)
