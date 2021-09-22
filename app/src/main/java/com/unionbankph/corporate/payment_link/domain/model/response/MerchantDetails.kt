package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantDetails (
    @SerialName("settlementAccountNumber")
    var settlementAccountNumber: String? = ""
)