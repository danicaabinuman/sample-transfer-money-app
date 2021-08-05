package com.unionbankph.corporate.payment_link.domain.model.response

import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkDraftCreatedBy
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkMerchantDetailsModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateMerchantByOrganizationResponse(
    @SerialName("merchantExists")
    var merchantExists: String? = null,

    @SerialName("merchantDetails")
    var merchantDetails: PaymentLinkMerchantDetailsModel? = null,

    @SerialName("merchantDraft")
    var merchantDraft: Boolean = false,

    @SerialName("draftCreatedBy")
    var draftCreatedBy: PaymentLinkDraftCreatedBy? = null,

    @SerialName("merchantStatus")
    var merchantStatus: String? = null

)
