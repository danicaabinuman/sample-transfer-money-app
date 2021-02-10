package com.unionbankph.corporate.settings.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPSettingsForm(

    @SerialName("login_otp")
    var loginOtp: Boolean = false,

    @SerialName("transaction_otp")
    var transactionOtp: Boolean = false
)
