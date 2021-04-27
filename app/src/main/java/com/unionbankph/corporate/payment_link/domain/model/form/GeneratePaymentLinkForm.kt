package com.unionbankph.corporate.payment_link.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GeneratePaymentLinkForm(
        @SerialName("amount")
        var totalAmount: Double = 0.0,
        @SerialName("description")
        var description: String,
        @SerialName("note")
        var notes: String? = null,
        @SerialName("expiry")
        var paymentLinkExpiry: Int = 12,
        @SerialName("mobileNumber")
        var mobileNumber: String?,
        @SerialName("organizationName")
        var organizationName: String?,
        @SerialName("corporateId")
        var corporateId: String?
)