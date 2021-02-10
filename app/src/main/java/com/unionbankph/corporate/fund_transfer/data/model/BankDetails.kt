package com.unionbankph.corporate.fund_transfer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BankDetails(

    @SerialName("name")
    val name: String? = null,

    @SerialName("address")
    val address: String? = null
) : Parcelable
