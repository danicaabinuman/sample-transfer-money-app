package com.unionbankph.corporate.request_payment_link.presentation.form

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.request_payment_link.data.RequestPayment
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class RequestPaymentViewModel
@Inject constructor(

) : BaseViewModel() {

    val requestPaymentAmount = BehaviorSubject.create<RequestPayment>()
}