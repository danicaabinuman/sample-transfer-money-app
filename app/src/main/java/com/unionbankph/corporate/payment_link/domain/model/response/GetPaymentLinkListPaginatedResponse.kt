package com.unionbankph.corporate.payment_link.domain.model.response

import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentLinkListPaginatedResponse(

    @SerialName("limit")
    var limit: Int? = null,

    @SerialName("totalCount")
    var totalCount: Int? = null,

    @SerialName("totalPage")
    var totalPage: Int? = null,

    @SerialName("page")
    var page: Int? = null,

    @SerialName("data")
    var data: List<PaymentLinkModel>? = null

)