package com.unionbankph.corporate.payment_link.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutPaymentLinkStatusForm(
    @SerialName("archive")
    var archive: String = "archived",
    @SerialName("unpaid")
    var unpaid: String = "unpaid"

)