package com.unionbankph.corporate.payment_link.domain.model.response

import com.unionbankph.corporate.payment_link.domain.model.PutPaymentLinkStatusDetailsModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutPaymentLinkStatusResponse(

    @SerialName("message")
    var message: String? = null,

    @SerialName("details")
    var details: PutPaymentLinkStatusDetailsModel? = null

)