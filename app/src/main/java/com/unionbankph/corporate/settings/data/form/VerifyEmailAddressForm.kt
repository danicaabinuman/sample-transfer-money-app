package com.unionbankph.corporate.settings.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailAddressForm(

    @SerialName("confirmation_id")
    var confirmationId: String? = null
)
