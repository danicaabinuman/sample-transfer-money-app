package com.unionbankph.corporate.payment_link.domain.model.form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UpdateSettlementOnRequestPaymentForm(

    @SerialName("accountNumber")
    var accountNumber: String

): Parcelable