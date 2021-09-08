package com.unionbankph.corporate.payment_link.domain.model.form

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class CreateMerchantForm(
        @SerialName("merchantName")
        var merchantName: String,
        @SerialName("uniqueStoreHandle")
        var uniqueStoreHandle: String,
        @SerialName("accountNo")
        var accountNo: String,
        @SerialName("accountName")
        var accountName: String,
        @SerialName("website")
        var website: String,
        @SerialName("productsAndServices")
        var productsAndServices: String
) : Parcelable