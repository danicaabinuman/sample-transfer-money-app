package com.unionbankph.corporate.bills_payment.presentation.biller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import javax.inject.Inject

class BillerMainViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider
) : BaseViewModel() {

    private val _billerState = MutableLiveData<BillerMainState>()

    val billerMainState: LiveData<BillerMainState> get() = _billerState
}

sealed class BillerMainState

data class ShowBillerMainError(val throwable: Throwable) : BillerMainState()
