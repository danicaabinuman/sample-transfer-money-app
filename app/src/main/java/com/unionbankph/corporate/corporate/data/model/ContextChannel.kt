package com.unionbankph.corporate.corporate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContextChannel(

    @SerialName("details")
    var details: MutableList<String> = mutableListOf(),

    @SerialName("group")
    var group: String? = null,

    @SerialName("display_name")
    var displayName: String? = null
)
