package com.unionbankph.corporate.fund_transfer.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLogDto(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("description")
    var description: String? = null,

    @SerialName("status")
    var status: ContextualClassStatus? = null,

    @SerialName("user_full_name")
    var userFullName: String? = null,

    @SerialName("action")
    var action: String? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null,

    @SerialName("reason_for_rejection")
    var reasonForRejection: String? = null,

    @SerialName("error_message")
    var errorMessage: String? = null
)
