package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPSettingsDto(

    @SerialName("login_otp")
    var loginOtp: Boolean = false,

    @SerialName("transaction_otp")
    var transactionOtp: Boolean = false
)
