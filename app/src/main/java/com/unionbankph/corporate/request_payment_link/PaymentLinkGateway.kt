package com.unionbankph.corporate.request_payment_link

import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import com.unionbankph.corporate.request_payment_link.data.model.RequestPaymentLinkResponse
import io.reactivex.Single

interface PaymentLinkGateway {
    fun requestPayment(requestPaymentForm: RequestPaymentForm) : Single<RequestPaymentLinkResponse>
}