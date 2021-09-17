package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreationOTPVerified (
    @SerialName("accessToken")
    var accessToken: String? = null
)