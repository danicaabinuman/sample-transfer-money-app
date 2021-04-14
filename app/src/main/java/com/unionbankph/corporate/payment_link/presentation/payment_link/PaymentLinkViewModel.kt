package com.unionbankph.corporate.payment_link.presentation.payment_link

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import javax.inject.Inject

class PaymentLinkViewModel
@Inject constructor(
        private val paymentLinkGateway: PaymentLinkGateway,
        private val schedulerProvider: SchedulerProvider
) : BaseViewModel(){

}