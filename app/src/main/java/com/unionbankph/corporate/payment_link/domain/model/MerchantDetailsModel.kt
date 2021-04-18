package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantDetailsModel(

    @SerialName("merchantName")
    var merchantName: String? = null,

    @SerialName("uniqueBusinessName")
    var uniqueBusinessName: String? = null,

    @SerialName("webpage")
    var webpage: String? = null,

    @SerialName("settlementAccountNumber")
    var settlementAccountNumber: String? = null,

    @SerialName("settlementAccountName")
    var settlementAccountName: String? = null

)