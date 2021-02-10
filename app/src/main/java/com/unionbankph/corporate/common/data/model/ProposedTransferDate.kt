package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProposedTransferDate(

    @SerialName("start_date")
    var startDate: String? = null,

    @SerialName("frequency")
    var frequency: String? = null,

    @SerialName("recurrence_type_id")
    var recurrenceTypeId: String? = null,

    @SerialName("occurrences")
    var occurrences: Int? = null,

    @SerialName("occurrences_text")
    var occurrencesText: String? = null,

    @SerialName("end_date")
    var endDate: String? = null,

    @SerialName("immediately")
    var immediately: Boolean? = null
)
