package com.unionbankph.corporate.request_payment_link.domain.form

import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class RequestPaymentForm(
        @SerialName("total_amount")
        var totalAmount: Double? = null,
        @SerialName("payment_for")
        var paymentFor: String? = null,
        @SerialName("notes")
        var notes: String? = null,
        @SerialName("payment_link_expiry")
        var paymentLinkExpiry: String? = null
) : Parcelable