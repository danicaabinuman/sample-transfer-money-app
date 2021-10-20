package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class PaymentLogsModel(

    @SerialName ("logId")
    var logId: Int? = null,

    @SerialName ("transactionId")
    var transactionId: String? = null,

    @SerialName ("referenceNo")
    var referenceNo: String? = null,

    @SerialName ("amount")
    var amount: Double? = null,

    @SerialName ("fee")
    var fee: Double? = null,

    @SerialName ("status")
    var status: String? = null,

    @SerialName ("paymentMethodSlug")
    var paymentMethodSlug: String? = null,

    @SerialName ("paymentMethodName")
    var paymentMethodName: String? = null,

    @SerialName ("createdDate")
    var createdDate: String? = null,

    @SerialName ("createdBy")
    var createdBy: String? = null,

    @SerialName ("modifiedDate")
    var modifiedDate: String? = null,

    @SerialName ("modifiedBy")
    var modifiedBy: String? = null

)
