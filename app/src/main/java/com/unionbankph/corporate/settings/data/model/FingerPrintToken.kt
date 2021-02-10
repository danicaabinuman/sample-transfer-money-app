package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FingerPrintToken(
    @SerialName("finger_print_token")
    var fingerPrintToken: String? = null
)
