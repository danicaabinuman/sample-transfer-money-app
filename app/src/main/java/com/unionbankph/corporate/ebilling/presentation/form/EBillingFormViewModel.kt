package com.unionbankph.corporate.ebilling.presentation.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald on 10/27/20
 */
class EBillingFormViewModel
@Inject constructor(

) : BaseViewModel() {

    private val _navigateNext = MutableLiveData<Event<String>>()

    val navigateNext: LiveData<Event<String>> get() = _navigateNext

    val selectedAccount = BehaviorSubject.create<Account>()

    val amount = BehaviorSubject.create<Double>()

    fun bindFreeFields(amount: Double) {
        this.amount.onNext(amount)
    }

    fun onClickedNext() {
        val eBillingForm = EBillingForm()
        eBillingForm.depositTo = selectedAccount.value
        eBillingForm.currency = selectedAccount.value?.currency
        eBillingForm.amount = amount.value
        _navigateNext.value = Event(JsonHelper.toJson(eBillingForm))
    }

}