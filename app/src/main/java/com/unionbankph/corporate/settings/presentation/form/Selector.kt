package com.unionbankph.corporate.settings.presentation.form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 2020-02-18
 */
@Parcelize
@Serializable
data class Selector(
    @SerialName("id")
    var id: String? = null,
    @SerialName("type")
    var type: String? = null,
    @SerialName("value")
    var value: String? = null
) : Parcelable
