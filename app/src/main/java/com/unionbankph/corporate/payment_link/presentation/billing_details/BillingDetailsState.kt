package com.unionbankph.corporate.payment_link.presentation.billing_details

import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import kotlinx.serialization.Serializable

@Serializable
data class BillingDetailsState (
    var paymentDetails: GetPaymentLinkByReferenceIdResponse? = null,
    var paymentLogs: MutableList<PaymentLogsModel>? = null
)