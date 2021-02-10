package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnabledFeaturesDto(
    @SerialName("enabled_features")
    var enabledfeatures: MutableList<String>? = null
)
