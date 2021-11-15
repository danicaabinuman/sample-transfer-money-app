package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MegaMenuDto(
    @SerialName("feature_code")
    val featureCode: String? = null,
    @SerialName("enabled")
    val isEnabled: Boolean? = null,
    @SerialName("custom_error_message")
    var errorMessage: String? = null
)