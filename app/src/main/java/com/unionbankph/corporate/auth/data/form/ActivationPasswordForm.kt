package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivationPasswordForm(

    @SerialName("activation_id")
    var activationId: String? = null
)
