package com.unionbankph.corporate.payment_link.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutPaymentLinkStatusForm(
    @SerialName("status")
    var status: String = "archived"

)