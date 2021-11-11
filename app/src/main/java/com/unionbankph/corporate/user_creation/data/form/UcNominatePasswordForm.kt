package com.unionbankph.corporate.user_creation.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UcNominatePasswordForm (
    @SerialName("password")
    var password: String? = null,
    @SerialName("secret_token")
    var secretToken: String? = null,
    @SerialName("request_id")
    var requestId: String? = null
)