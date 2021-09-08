package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentLinkDraftCreatedBy(
    @SerialName("corporateId")
    var corporateId: String? = null,

    @SerialName("firstName")
    var firstName: String? = null,

    @SerialName("lastName")
    var lastName: String? = null
)