package com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class FrequentBillerViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway
) : BaseViewModel() {

    private val _state = MutableLiveData<FrequentBillerState>()

    private val _frequentBillers = MutableLiveData<MutableList<FrequentBiller>>()

    val state: LiveData<FrequentBillerState> get() = _state

    val frequentBillers: LiveData<MutableList<FrequentBiller>> get() = _frequentBillers

    private var getFrequentBillersDisposable: Disposable? = null

    fun getFrequentBillers(
        accountId: String? = null,
        pageable: Pageable,
        isInitialLoading: Boolean
    ) {
        getFrequentBillersDisposable?.dispose()
        getFrequentBillersDisposable =
            billsPaymentGateway.getFrequentBillers(accountId, pageable)
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                }
                .map { pageable.combineList(frequentBillers.value, it.results) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe {
                    _state.value =
                        if (isInitialLoading)
                            ShowFrequentBillerLoading
                        else
                            ShowFrequentBillerEndlessLoading
                }
                .doFinally {
                    _state.value =
                        if (isInitialLoading)
                            ShowFrequentBillerDismissLoading
                        else
                            ShowFrequentBillerEndlessDismissLoading
                }
                .subscribe(
                    {
                        _frequentBillers.value = it
                    }, {
                        Timber.e(it, "Get account failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _state.value = ShowFrequentBillerError(it)
                        }
                    })
        getFrequentBillersDisposable?.addTo(disposables)
    }

    fun getFrequentBillersTestData() {
        billsPaymentGateway.getFrequentBillersTestData()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _state.value = ShowFrequentBillerGetTestDataList(it.results)
                }, {
                    Timber.e(it, "getFrequentBillersTestData failed")
                    _state.value = ShowFrequentBillerError(it)
                })
            .addTo(disposables)
    }
}

sealed class FrequentBillerState

object ShowFrequentBillerLoading : FrequentBillerState()

object ShowFrequentBillerEndlessLoading : FrequentBillerState()

object ShowFrequentBillerDismissLoading : FrequentBillerState()

object ShowFrequentBillerEndlessDismissLoading : FrequentBillerState()

data class ShowFrequentBillerGetTestDataList(
    val data: MutableList<FrequentBiller>
) : FrequentBillerState()

data class ShowFrequentBillerError(val throwable: Throwable) : FrequentBillerState()
