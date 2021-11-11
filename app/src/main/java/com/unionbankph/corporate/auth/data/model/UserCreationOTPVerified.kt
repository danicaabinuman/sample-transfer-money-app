package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreationOTPVerified (
    @SerialName("secret_token")
    var accessToken: String? = null,
    @SerialName("request_id")
    var requestId: String? = null,
)