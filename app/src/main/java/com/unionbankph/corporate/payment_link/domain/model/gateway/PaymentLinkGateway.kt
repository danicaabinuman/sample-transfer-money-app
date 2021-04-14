package com.unionbankph.corporate.payment_link.domain.model.gateway

import com.unionbankph.corporate.payment_link.domain.model.response.AllPaymentLinksResponse
import com.unionbankph.corporate.payment_link.domain.model.form.AllPaymentLinksForm
import io.reactivex.Single

interface PaymentLinkGateway {
    fun allPaymentLinksGateway(allPaymentLinksForm: AllPaymentLinksForm) : Single<AllPaymentLinksResponse>
}