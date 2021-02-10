package com.unionbankph.corporate.general.data.model

import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.settings.presentation.form.Selector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionFilterForm(

    @SerialName("start_date")
    var startDate: String? = null,

    @SerialName("end_date")
    var endDate: String? = null,

    @SerialName("channels")
    var channels: String? = null,

    @SerialName("statuses")
    var statuses: String? = null,

    @SerialName("channels_value_to_display")
    var channelsValueToDisplay: String? = null,

    @SerialName("statuses_value_to_display")
    var statusesValueToDisplay: String? = null,

    @SerialName("channels_state_data")
    var channelsStateData: MutableList<StateData<Selector>>? = null,

    @SerialName("statuses_state_data")
    var statusesStateData: MutableList<StateData<Selector>>? = null,

    @SerialName("count")
    var count: Long? = null,

    @SerialName("biller")
    var biller: Biller? = null
)
