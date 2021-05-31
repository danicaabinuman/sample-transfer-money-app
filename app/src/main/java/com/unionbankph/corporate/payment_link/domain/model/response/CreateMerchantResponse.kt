package com.unionbankph.corporate.payment_link.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMerchantResponse(
        @SerialName("message")
        var message: String? = null,

        @SerialName("merchantName")
        val merchantName: String? = null,

        @SerialName("slug")
        var slug: String? = null,

        @SerialName("accountNo")
        var accountNo: String? = null,

        @SerialName("accountName")
        var accountName: String? = null,

        @SerialName("status")
        var status: String? = null,

        @SerialName("webpage")
        var webpage: String? = null,

        @SerialName("product")
        var product: String? = null,

        @SerialName("httpStatus")
        var httpStatus: String? = null,

        @SerialName("timestamp")
        var timestamp: String? = null
)
