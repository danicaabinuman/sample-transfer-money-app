package com.unionbankph.corporate.account.presentation.account_history_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.form.GetAccountTransactionHistoryDetailsForm
import com.unionbankph.corporate.account.domain.interactor.GetAccountTransactionHistoryDetails
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject


class AccountTransactionHistoryDetailsViewModel @Inject constructor(
    private val getAccountTransactionHistoryDetails: GetAccountTransactionHistoryDetails
) : BaseViewModel() {

    private val _accountTransactionHistoryDetails =
        MutableLiveData<AccountTransactionHistoryDetails>()

    val accountTransactionHistoryDetails: LiveData<AccountTransactionHistoryDetails>
        get() =
            _accountTransactionHistoryDetails

    private val _navigateViewTransaction = MutableLiveData<Event<String>>()

    val navigateViewTransaction: LiveData<Event<String>> get() = _navigateViewTransaction

    val record = BehaviorSubject.create<Record>()
    val accountId = BehaviorSubject.create<String>()

    fun initBundleData(record: String?, id: String?) {
        id?.let { accountId.onNext(it) }
        Single.just(record)
            .map { JsonHelper.fromJson<Record>(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    this.record.onNext(it)
                    getAccountTransactionHistoryDetails()
                }, {
                    Timber.e(it, "setupRecord")
                }
            ).addTo(disposables)
    }

    private fun getAccountTransactionHistoryDetails() {
        val getAccountTransactionHistoryDetailsForm =
            GetAccountTransactionHistoryDetailsForm().apply {
                id = accountId.value
                referenceNumber = record.value?.tranId
                serialNo = record.value?.serialNo
                transactionDate = record.value?.tranDate.convertDateToDesireFormat(
                    DateFormatEnum.DATE_FORMAT_ISO_DATE
                )
            }
        getAccountTransactionHistoryDetails.execute(
            getDisposableSingleObserver(
                {
                    _accountTransactionHistoryDetails.value = it
                }, {
                    Timber.e(it, "getAccountTransactionHistoryDetails")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = getAccountTransactionHistoryDetailsForm
        ).addTo(disposables)
    }

    fun onClickedViewTransaction() {
        _navigateViewTransaction.value =
            Event(JsonHelper.toJson(accountTransactionHistoryDetails.value?.portalTransaction))
    }

}

