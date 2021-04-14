package com.unionbankph.corporate.request_payment_link

import com.unionbankph.corporate.payment_link.domain.model.form.RequestPaymentForm
import com.unionbankph.corporate.payment_link.domain.model.response.RequestPaymentLinkResponse
import io.reactivex.Single

interface RequestPaymentGateway {
    fun requestPayment(requestPaymentForm: RequestPaymentForm) : Single<RequestPaymentLinkResponse>
}