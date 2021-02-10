package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OccupationDto(
    @SerialName("finacle_code")
    var finacleCode: String? = null,
    @SerialName("name")
    var name: String? = null
)
