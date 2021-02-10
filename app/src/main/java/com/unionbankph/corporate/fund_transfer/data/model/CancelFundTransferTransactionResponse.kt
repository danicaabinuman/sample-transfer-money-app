package com.unionbankph.corporate.fund_transfer.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelFundTransferTransactionResponse(

    @SerialName("message")
    var message: String? = null,

    @SerialName("contextual_class")
    var contextualClass: ContextualClassStatus? = null
)
