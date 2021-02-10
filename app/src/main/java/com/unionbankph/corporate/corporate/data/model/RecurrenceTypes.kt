package com.unionbankph.corporate.corporate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecurrenceTypes(

    @SerialName("id")
    var id: String? = null,

    @SerialName("name")
    var name: String? = null
)
