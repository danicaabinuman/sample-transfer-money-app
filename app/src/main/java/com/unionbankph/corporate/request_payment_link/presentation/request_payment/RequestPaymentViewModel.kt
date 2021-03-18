package com.unionbankph.corporate.request_payment_link.presentation.request_payment

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.request_payment_link.RequestPaymentGateway
import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import javax.inject.Inject

class RequestPaymentViewModel @Inject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val requestPaymentGateway: RequestPaymentGateway
        ) : BaseViewModel() {

    fun requestLink(requestPaymentForm: RequestPaymentForm) {
        requestPaymentGateway.requestPayment(requestPaymentForm)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe()

    }

}