package com.unionbankph.corporate.request_payment_link.presentation

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.request_payment_link.data.RequestPayment
import io.reactivex.subjects.BehaviorSubject

class RequestPaymentViewModel : BaseViewModel() {

    val requestPaymentAmount = BehaviorSubject.create<RequestPayment>()
}