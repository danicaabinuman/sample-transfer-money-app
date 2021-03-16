package com.unionbankph.corporate.request_payment_link.data.form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class RequestPaymentForm(
        @SerialName("amount")
        var totalAmount: Double? = null,
        @SerialName("description")
        var description: String? = null,
        @SerialName("note")
        var notes: String? = null,
        @SerialName("expiry")
        var paymentLinkExpiry: String? = null
) : Parcelable