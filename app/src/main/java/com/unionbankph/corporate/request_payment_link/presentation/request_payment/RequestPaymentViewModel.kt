package com.unionbankph.corporate.request_payment_link.presentation.request_payment

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.flatMapIf
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.request_payment_link.PaymentLinkGateway
import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import javax.inject.Inject

class RequestPaymentViewModel @Inject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val paymentLinkGateway: PaymentLinkGateway
        ) : BaseViewModel() {

    fun requestLink(requestPaymentForm: RequestPaymentForm) {
        paymentLinkGateway.requestPayment(requestPaymentForm)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe()

    }

}