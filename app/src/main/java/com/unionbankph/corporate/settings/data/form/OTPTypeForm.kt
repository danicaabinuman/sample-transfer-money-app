package com.unionbankph.corporate.settings.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPTypeForm(

    @SerialName("login_type")
    var loginType: String? = null
)
