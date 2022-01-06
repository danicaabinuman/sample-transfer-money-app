package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProvincesDtoResponse(
    val records: MutableList<ProvinceDto>
)
