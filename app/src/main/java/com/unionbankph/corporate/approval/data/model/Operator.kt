package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Operator(
    
    @SerialName("id")
    var id: Int? = null,
    
    @SerialName("threshold")
    var threshold: Int? = null,
    
    @SerialName("children")
    var children: MutableList<Children>? = null
)