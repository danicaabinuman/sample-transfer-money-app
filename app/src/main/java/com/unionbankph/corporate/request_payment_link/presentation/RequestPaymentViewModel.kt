package com.unionbankph.corporate.request_payment_link.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import com.unionbankph.corporate.request_payment_link.domain.form.RequestPaymentForm
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald on 10/27/20
 */
class RequestPaymentViewModel
@Inject constructor(

) : BaseViewModel() {

    private val _navigateGenerate = MutableLiveData<Event<String>>()

    val navigateGenerate: LiveData<Event<String>> get() = _navigateGenerate

    val selectedAccount = BehaviorSubject.create<Account>()

    val amount = BehaviorSubject.create<Double>()

    fun bindFreeFields(amount: Double) {
        this.amount.onNext(amount)
    }

    fun onClickedGenerate() {
        val requestPaymentForm = RequestPaymentForm()
        requestPaymentForm.totalAmount = 0.0
        requestPaymentForm.paymentFor = "TEST"
        _navigateGenerate.value = Event(JsonHelper.toJson(requestPaymentForm))
    }

}