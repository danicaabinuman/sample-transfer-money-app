package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentLinkMerchantDetailsModel(
    @SerialName("settlementAccountNumber")
    var settlementAccountNumber: String? = null,

    @SerialName("settlementAccountId")
    var settlementAccountId: String? = null,

    @SerialName("hasExistingCard")
    var hasExistingCard: Boolean = false,

    @SerialName("segmentThree")
    var segmentThree: Boolean = false
)
