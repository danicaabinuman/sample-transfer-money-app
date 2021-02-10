package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPTypeDto(

    @SerialName("login_type")
    var loginType: String? = null
)
