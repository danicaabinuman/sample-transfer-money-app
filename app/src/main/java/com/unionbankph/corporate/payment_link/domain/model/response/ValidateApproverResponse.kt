package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateApproverResponse(
    @SerialName("isApprover")
    var isApprover: Boolean
)
