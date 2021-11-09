package com.unionbankph.corporate.instapay_qr.domain.model.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateFTForm(
    @SerialName("qrContent")
    var qrContent: String? = null
)