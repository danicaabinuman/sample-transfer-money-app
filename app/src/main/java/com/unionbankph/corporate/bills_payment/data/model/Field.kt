package com.unionbankph.corporate.bills_payment.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Field(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("index")
    var index: Int? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("value")
    var value: String? = null
) : Parcelable
