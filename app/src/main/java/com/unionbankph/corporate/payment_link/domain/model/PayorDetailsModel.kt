package com.unionbankph.corporate.payment_link.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PayorDetailsModel(

    @SerialName("fullName")
    var fullName: String? = null,

    @SerialName("emailAddress")
    var emailAddress: String? = null,

    @SerialName("paymentMethod")
    var paymentMethod: String? = null,

    @SerialName("mobileNumber")
    var mobileNumber: String? = null

)