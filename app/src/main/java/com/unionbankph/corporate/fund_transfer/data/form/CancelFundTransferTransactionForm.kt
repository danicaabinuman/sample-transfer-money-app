package com.unionbankph.corporate.fund_transfer.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelFundTransferTransactionForm(

    @SerialName("batch_id")
    var batchId: String? = null,

    @SerialName("reason_for_cancellation")
    var reasonForCancellation: String? = null
)
