package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchVisitSubmitDto(

    @SerialName("status")
    var status: String? = null,

    @SerialName("reference_id")
    var referenceId: String? = null
)
