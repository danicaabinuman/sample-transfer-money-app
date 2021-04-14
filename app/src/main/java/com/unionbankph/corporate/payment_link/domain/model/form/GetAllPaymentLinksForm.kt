package com.unionbankph.corporate.payment_link.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAllPaymentLinksForm(
    @SerialName("page")
    var page: Int = 1,
    @SerialName("items-per-page")
    var itemsPerPage: Int = 10,
    @SerialName("reference-number")
    var referenceNumber: String? = null
)