package com.unionbankph.corporate.request_payment_link.presentation.payment_link

import com.unionbankph.corporate.request_payment_link.data.AllPaymentLinksResponse
import com.unionbankph.corporate.request_payment_link.data.form.AllPaymentLinksForm
import io.reactivex.Single

interface PaymentLinkGateway {
    fun allPaymentLinksGateway(allPaymentLinksForm: AllPaymentLinksForm) : Single<AllPaymentLinksResponse>
}