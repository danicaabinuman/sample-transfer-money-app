package com.unionbankph.corporate.payment_link.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentLinkByReferenceIdForm(
    @SerialName("reference-id")
    var referenceId: String? = null
)