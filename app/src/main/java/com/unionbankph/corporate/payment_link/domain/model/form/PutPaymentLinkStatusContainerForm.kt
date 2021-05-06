package com.unionbankph.corporate.payment_link.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PutPaymentLinkStatusContainerForm(
    var transactionId: String,
    var putPaymentLinkStatusForm: PutPaymentLinkStatusForm
)