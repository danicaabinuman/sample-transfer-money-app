package com.unionbankph.corporate.link_details.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkDetailsResponse(

    @SerialName("payment_link")
    var link: String? = null,

    @SerialName("ref_no")
    var referenceId: String? = null,

    @SerialName("amount")
    var amount: String = "0",

    @SerialName("description")
    var description: String? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("payment_for")
    var paymentFor: String? = null,

    @SerialName("expiry")
    var expireDate: String? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null






)