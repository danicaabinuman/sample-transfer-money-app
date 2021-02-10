package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ECredMergeSubmitDto(

    @SerialName("requestId")
    var requestId: String? = null,

    @SerialName("mobileNumber")
    var mobileNumber: String? = null,

    @SerialName("validity")
    var validity: Int? = null
)
