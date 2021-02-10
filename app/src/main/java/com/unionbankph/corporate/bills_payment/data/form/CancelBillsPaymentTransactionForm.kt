package com.unionbankph.corporate.bills_payment.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelBillsPaymentTransactionForm(

    @SerialName("batch_id")
    var batchId: String? = null,

    @SerialName("reason_for_cancellation")
    var reasonForRejection: String? = null
)
