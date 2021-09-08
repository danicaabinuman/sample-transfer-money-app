package com.unionbankph.corporate.settings.data.form

import kotlinx.serialization.*

@Serializable
data class OTPSettingsForm(

    @SerialName("login_otp")
    var loginOtp: Boolean? = null,

    @SerialName("transaction_otp")
    var transactionOtp: Boolean? = null
)
