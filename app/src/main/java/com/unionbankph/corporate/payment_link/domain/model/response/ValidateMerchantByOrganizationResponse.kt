package com.unionbankph.corporate.payment_link.domain.model.response

import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkDraftCreatedBy
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkMerchantDetailsModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateMerchantByOrganizationResponse(
    @SerialName("merchantExists")
    var merchantExists: String,

    @SerialName("merchantDetails")
    var merchantDetails: PaymentLinkMerchantDetailsModel,

    @SerialName("merchantDraft")
    var merchantDraft: Boolean = false,

    @SerialName("draftCreatedBy")
    var draftCreatedBy: PaymentLinkDraftCreatedBy,

    @SerialName("merchantStatus")
    var merchantStatus: Boolean = false

)
