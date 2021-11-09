package com.unionbankph.corporate.instapay_qr.domain.model

import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.instapay_qr.domain.model.response.BankData
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuccessQRReference(
    var channel: Channel? = null,
    var recipientData: GenerateFTResponse? = null,
    var hasValidationError: Boolean? = null
)
