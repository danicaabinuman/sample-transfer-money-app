package com.unionbankph.corporate.account.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Headers(

    @SerialName("name")
    val name: String? = null,

    @SerialName("display")
    val display: String? = null,

    @SerialName("value")
    val value: String? = null
) : Parcelable
