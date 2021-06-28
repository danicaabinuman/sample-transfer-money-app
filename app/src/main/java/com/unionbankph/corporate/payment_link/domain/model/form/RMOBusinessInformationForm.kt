package com.unionbankph.corporate.payment_link.domain.model.form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class RMOBusinessInformationForm {
    @SerialName("businessType")
    var businessType: String,
    @SerialName("product")
    var product: String,

} : Parcelable