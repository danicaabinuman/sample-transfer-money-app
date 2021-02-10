package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TOTPSubscribeDto(

    @SerialName("token")
    var token: String? = null
)
