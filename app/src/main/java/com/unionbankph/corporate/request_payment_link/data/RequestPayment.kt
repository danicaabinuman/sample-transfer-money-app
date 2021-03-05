package com.unionbankph.corporate.request_payment_link.data

import kotlinx.serialization.SerialName

class RequestPayment {

    @SerialName("datetime")
    var datetime: String? = null

    @SerialName("status")
    var status: Int? = null

    @SerialName("message")
    var message: String? = null

    @SerialName("path")
    var path: String? = null
}