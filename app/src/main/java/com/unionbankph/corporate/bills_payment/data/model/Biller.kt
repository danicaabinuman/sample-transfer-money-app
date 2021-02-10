package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Biller(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("service_id")
    var serviceId: String? = null,

    @SerialName("short_name")
    var shortName: String? = null,

    @SerialName("can_add_as_frequent_biller")
    var canAddAsFrequentBiller: Boolean = true
)
