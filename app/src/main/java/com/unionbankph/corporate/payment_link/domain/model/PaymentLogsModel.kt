package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName

data class PaymentLogsModel(

    @SerialName ("loginId")
    var loginId: String? = null,

    @SerialName ("transactionId")
    var transactionId: String? = null,

    @SerialName ("referenceNo")
    var referenceNo: String? = null,

    @SerialName ("fee")
    var fee: String? = null,

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
