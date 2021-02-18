package com.unionbankph.corporate.request_payment_link.data

import kotlinx.serialization.SerialName

class RequestPayment {

    @SerialName("check_amount")
    var checkAmount: String? = null
}