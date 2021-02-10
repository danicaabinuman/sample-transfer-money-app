package com.unionbankph.corporate.fund_transfer.presentation.swift_bank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class SwiftBankViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway
) : BaseViewModel() {

    private val _swiftBanks = MutableLiveData<MutableList<SwiftBank>>()

    val swiftBanks: LiveData<MutableList<SwiftBank>> get() = _swiftBanks

    val pageable = Pageable()

    private var getSwiftBanksDisposable: Disposable? = null

    fun getSwiftBanks(isInitialLoading: Boolean) {
        getSwiftBanksDisposable?.dispose()
        getSwiftBanksDisposable = fundTransferGateway.getSwiftBanks(pageable)
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    increasePage()
                }
            }
            .map { pageable.combineList(swiftBanks.value, it.results) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                if (!isInitialLoading) {
                    pageable.isLoadingPagination = true
                }
                _uiState.value = Event(UiState.Loading)
            }
            .doFinally {
                if (!isInitialLoading) {
                    pageable.isLoadingPagination = false
                }
                _uiState.value = Event(UiState.Complete)
            }
            .subscribe(
                {
                    _swiftBanks.value = it
                }, {
                    Timber.e(it, "getSwiftBanks Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _uiState.value = Event(UiState.Error(it))
                    }
                }
            )
        getSwiftBanksDisposable?.addTo(disposables)
    }
}
