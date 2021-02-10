package com.unionbankph.corporate.bills_payment.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelBillsPaymentTransactionResponse(

    @SerialName("message")
    var message: String? = null,

    @SerialName("contextual_class")
    var contextualClass: ContextualClassStatus? = null
)
