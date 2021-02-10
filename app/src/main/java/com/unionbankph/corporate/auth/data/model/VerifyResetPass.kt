package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyResetPass(
    @SerialName("reset_pw_token_id")
    val resetPWTokenId: String
)
