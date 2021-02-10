package com.unionbankph.corporate.approval.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalHierarchy(
    
    @SerialName("id")
    var id: String? = null,
    
    @SerialName("name")
    var name: String? = null,
    
    @SerialName("root")
    var root: Root? = null,
    
    @SerialName("schema_id")
    var schemaId: String? = null
)