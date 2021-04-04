package com.unionbankph.corporate.request_payment_link.data.form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class CreateMerchantForm(
        @SerialName("organizationId")
        var organizationId: String,
        @SerialName("merchantName")
        var merchantName: String,
        @SerialName("uniqueStoreHandle")
        var uniqueStoreHandle: String,
        @SerialName("accountNo")
        var accountNo: String,
        @SerialName("website")
        var website: String,
        @SerialName("productsAndServices")
        var productsAndServices: String
) : Parcelable