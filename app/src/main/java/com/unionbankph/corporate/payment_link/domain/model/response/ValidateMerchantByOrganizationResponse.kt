package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateMerchantByOrganizationResponse(
    @SerialName("merchantExists")
    var merchantExists: String,
    @SerialName("merchantDetails")
    var merchantDetails: MerchantDetails? = null
)
