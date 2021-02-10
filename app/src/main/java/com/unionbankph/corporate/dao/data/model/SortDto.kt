package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SortDto(
    @SerialName("direction")
    var direction: String? = null,
    @SerialName("property")
    var property: String? = null,
    @SerialName("ignoreCase")
    var ignoreCase: Boolean? = null,
    @SerialName("nullHandling")
    var nullHandling: String? = null,
    @SerialName("descending")
    var descending: Boolean? = null,
    @SerialName("ascending")
    var ascending: Boolean? = null
)
