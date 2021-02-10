package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledTransferDeletionForm(
    @SerialName("batch_ids")
    var batchIds: MutableList<Long> = mutableListOf(),
    @SerialName("reason_for_cancellation")
    var reasonForCancellation: String? = null
)
