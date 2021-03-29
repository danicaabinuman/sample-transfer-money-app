package com.unionbankph.corporate.link_details.data.form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LinkDetailsForm(
        @SerialName("amount")
        var totalAmount: Double = 0.0,
        @SerialName("description")
        var description: String,
        @SerialName("note")
        var notes: String? = null,
        @SerialName("expiry")
        var paymentLinkExpiry: Int = 12
)