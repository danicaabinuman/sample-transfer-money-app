package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordForm(

    @SerialName("old_password")
    var oldPassword: String? = null,

    @SerialName("new_password")
    var newPassword: String? = null,

    @SerialName("confirm_new_password")
    var confirmNewPassword: String? = null
)
