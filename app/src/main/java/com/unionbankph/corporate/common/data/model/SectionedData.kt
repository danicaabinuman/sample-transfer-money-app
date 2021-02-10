package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SectionedData<T>(
    @SerialName("type")
    var type: String? = null,
    @SerialName("header")
    var header: String? = null,
    @SerialName("data")
    var data: MutableList<T> = mutableListOf()
)
