package com.unionbankph.corporate.payment_link.domain.model.response

import com.unionbankph.corporate.payment_link.domain.model.MerchantDetailsModel
import com.unionbankph.corporate.payment_link.domain.model.PaymentDetailsModel
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import com.unionbankph.corporate.payment_link.domain.model.PayorDetailsModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentLinkByReferenceIdResponse(

    @SerialName("merchantId")
    var merchantId: Int? = null,

    @SerialName("transactionId")
    var transactionId: String? = null,

    @SerialName("instapayTransactionId")
    var instapayTransactionId: String? = null,

    @SerialName("ubOnlineTransactionId")
    var ubOnlineTransactionId: String? = null,

    @SerialName("referenceNo")
    var referenceNo: String? = null,

    @SerialName("paymentDetails")
    var paymentDetails: PaymentDetailsModel? = null,

    @SerialName("merchantDetails")
    var merchantDetails: MerchantDetailsModel? = null,

    @SerialName("payorDetails")
    var payorDetails: PayorDetailsModel? = null,

    @SerialName("expiry")
    var expiry: String? = null,

    @SerialName("createdDate")
    var createdDate: String? = null

)