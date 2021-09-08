package com.unionbankph.corporate.auth.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class CountryCode(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("code")
    val code: String? = null,

    @SerialName("status")
    val status: Int? = null,

    @SerialName("preferred")
    val preferred: Int? = null,

    @SerialName("calling_code")
    val callingCode: String? = null
) : Parcelable
