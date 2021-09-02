package com.unionbankph.corporate.settings.presentation.form

import android.os.Parcelable
import com.unionbankph.corporate.common.data.model.StateData
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 2020-02-18
 */
@Parcelize
@Serializable
data class SelectorData(
    @SerialName("value_to_display")
    var valueToDisplay: String? = null,
    @SerialName("items")
    var items: String? = null,
    @SerialName("selectors")
    var selectors: MutableList<Selector>? = null,
    @SerialName("state_data")
    var stateData: MutableList<StateData<Selector>>? = null
) : Parcelable
