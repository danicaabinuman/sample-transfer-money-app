package com.unionbankph.corporate.mcd.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckDepositActivityLog(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("status")
    var status: ContextualClassStatus? = null,

    @SerialName("reason_for_rejection")
    var reasonForRejection: String? = null,

    @SerialName("error_message")
    var errorMessage: String? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null
)
