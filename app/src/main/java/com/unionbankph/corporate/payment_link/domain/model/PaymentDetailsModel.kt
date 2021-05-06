package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDetailsModel(

    @SerialName("paymentFor")
    var paymentFor: String? = null,

    @SerialName("description")
    var description: String? = null,

    @SerialName("amount")
    var amount: String = "0",

    @SerialName("paymentLink")
    var paymentLink: String? = null,

    @SerialName("referenceNo")
    var referenceNo: String? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("upacTransacStatus")
    var upacTransacStatus: String? = null,

    @SerialName("fee")
    var fee: String? = null,

    @SerialName("settlementDate")
    var settlementDate: String? = null,

    @SerialName("paymentDate")
    var paymentDate: String? = null,

    @SerialName("settledDate")
    var settledDate: String? = null

)