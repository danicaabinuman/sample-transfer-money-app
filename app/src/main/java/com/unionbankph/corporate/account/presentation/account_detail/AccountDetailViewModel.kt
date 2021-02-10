package com.unionbankph.corporate.account.presentation.account_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class AccountDetailViewModel @Inject constructor(
    private val accountGateway: AccountGateway
) : BaseViewModel() {

    private val _records = MutableLiveData<MutableList<Record>>()

    val records: LiveData<MutableList<Record>> get() = _records

    fun getRecentTransactions(id: String, limit: Int) {
        accountGateway.getRecentTransactionsWithPagination(id, limit)
            .map { it.records }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _records.value = it
                }, {
                    Timber.e(it, "getRecentTransactionsWithPagination Failed")
                    _uiState.value = Event(UiState.Complete)
                }
            )
            .addTo(disposables)
    }

}
