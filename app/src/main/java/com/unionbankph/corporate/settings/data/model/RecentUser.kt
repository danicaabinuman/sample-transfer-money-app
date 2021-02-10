package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentUser(

    @SerialName("user_email")
    var userEmail: String? = null,

    @SerialName("is_ask_trust_device")
    var isAskTrustDevice: Boolean? = null,

    @SerialName("is_ask_biometric")
    var isAskBiometric: Boolean? = null
)
