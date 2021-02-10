package com.unionbankph.corporate.approval.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Root(
    
    @SerialName("id")
    var id: Int? = null,
    
    @SerialName("type")
    var type: String? = null,
    
    @SerialName("group")
    var group: Group? = null,
    
    @SerialName("operator")
    var operator: Operator? = null,
    
    @SerialName("next_step")
    var nextStep: NextStep? = null,
    
    @SerialName("number_of_approvers")
    var numberOfApprovers: Int? = null,
    
    @SerialName("node_status")
    var nodeStatus: ContextualClassStatus? = null
)