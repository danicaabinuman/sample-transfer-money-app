package com.unionbankph.corporate.common.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class StateData<MODEL : Parcelable>(
    @SerialName("data")
    var data: MODEL,
    @SerialName("state")
    var state: Boolean = false
) : Parcelable
