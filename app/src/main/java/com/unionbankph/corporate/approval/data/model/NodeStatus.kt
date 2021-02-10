package com.unionbankph.corporate.approval.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NodeStatus(
    @SerialName("type")
    val type: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("contextual_class")
    val contextualClass: String? = null
)