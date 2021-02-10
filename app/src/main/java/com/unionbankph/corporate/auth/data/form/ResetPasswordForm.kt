package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordForm(

    @SerialName("password_token")
    var passwordToken: String? = null,

    @SerialName("password")
    var password: String? = null,

    @SerialName("password_confirm")
    var passwordConfirm: String? = null,

    @SerialName("reset_pw_token_id")
    var resetPwTokenId: String? = null
)
