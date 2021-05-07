package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentLinkModel(

    @SerialName("transactionId")
    var transactionId: String? = null,

    @SerialName("referenceNo")
    var referenceNo: String? = null,

    @SerialName("amount")
    var amount: String = "0",

    @SerialName("paymentFor")
    var paymentFor: String? = null,

    @SerialName("note")
    var note: String? = null,

    @SerialName("expireDate")
    var expireDate: String? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("mobileNumber")
    var mobileNumber: String? = null,

    @SerialName("paymentLink")
    var paymentLink: String? = null,

    @SerialName("createdDate")
    var createdDate: String? = null,

    @SerialName("isCustomerConsent")
    var isCustomerConsent: String? = null,

    @SerialName("modifiedDate")
    var modifiedDate: String? = null,

    @SerialName("paymentDate")
    var paymentDate: String? = null,

    @SerialName("settlementDate")
    var settlementDate: String? = null,

    @SerialName("settledDate")
    var settledDate: String? = null





)