package com.unionbankph.corporate.bills_payment.presentation.biller.biller_all

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class AllBillerViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway
) : BaseViewModel() {

    private val _billerState = MutableLiveData<AllBillerState>()

    var billerHeader: MutableList<Biller>? = ArrayList()

    val allBillerState: LiveData<AllBillerState> get() = _billerState

    fun getBillers(type: String) {
        billsPaymentGateway.getBillers(type)
            .map { list ->
                billerHeader = list.asSequence()
                    .distinctBy { Pair(it.name?.get(0), it.name?.get(0)) }
                    .toMutableList()
                Pair(billerHeader, list)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _billerState.value = ShowAllBillerLoading }
            .doFinally { _billerState.value = ShowAllBillerDismissLoading }
            .subscribe(
                {
                    _billerState.value =
                        ShowAllBillerSuccess(it.first ?: mutableListOf(), it.second)
                }, {
                    Timber.e(it, "getPesoNetBanks Failed")
                    _billerState.value = ShowAllBillerError(it)
                }).addTo(disposables)
    }
}

sealed class AllBillerState

object ShowAllBillerLoading : AllBillerState()

object ShowAllBillerDismissLoading : AllBillerState()

data class ShowAllBillerSuccess(
    val listHeader: MutableList<Biller>,
    val list: MutableList<Biller>
) : AllBillerState()

data class ShowAllBillerError(val throwable: Throwable) : AllBillerState()
