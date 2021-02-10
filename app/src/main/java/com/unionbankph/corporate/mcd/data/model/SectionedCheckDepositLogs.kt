package com.unionbankph.corporate.mcd.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SectionedCheckDepositLogs(
    @SerialName("header")
    var header: String? = null,
    @SerialName("check_deposit_activity_logs")
    var checkDepositActivityLogs: MutableList<CheckDepositActivityLog>? = null
)
