package com.unionbankph.corporate.approval.presentation.approval_done

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatAmount
import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.CurrencyEnum
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject


class ApprovalDoneViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val approvalGateway: ApprovalGateway
) : BaseViewModel() {

    private val _approvalState = MutableLiveData<ApprovalDoneState>()

    private val _transactions = MutableLiveData<MutableList<Transaction>>()

    val state: LiveData<ApprovalDoneState> = _approvalState

    val transactions: LiveData<MutableList<Transaction>> = _transactions

    private var getApprovalListDisposable: Disposable? = null

    fun getApprovalList(status: String, pageable: Pageable, isInitialLoading: Boolean) {
        getApprovalListDisposable?.dispose()
        getApprovalListDisposable =
            approvalGateway.getApprovalList(pageable, status)
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                }
                .toObservable()
                .flatMapIterable { it.results }
                .map {
                    if (ChannelBankEnum.CHECK_WRITER.value == it.channel) {
                        mapTransactionDetails(it)
                    } else {
                        it
                    }
                }
                .toList()
                .map { pageable.combineList(_transactions.value, it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe {
                    _approvalState.value =
                        if (isInitialLoading)
                            ShowApprovalDoneLoading
                        else
                            ShowApprovalDoneEndlessLoading
                }
                .doFinally {
                    _approvalState.value =
                        if (isInitialLoading)
                            ShowApprovalDoneDismissLoading
                        else
                            ShowApprovalDoneDismissEndlessLoading
                }
                .subscribe(
                    {
                        _transactions.value = it
                    }, {
                        Timber.e(it, "getApprovalList failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _approvalState.value = ShowApprovalDoneError(it)
                        }
                    })
        getApprovalListDisposable?.addTo(disposables)
    }

    private fun mapTransactionDetails(transaction: Transaction): Transaction {
        transaction.id = transaction.transactionReferenceNumber
        transaction.channel = ChannelBankEnum.CHECK_WRITER.value
        Observable.fromIterable(transaction.transactionDetails)
            .forEach {
                when (it.header) {
                    "remarks" -> transaction.remarks = it.value
                    "batch_type" -> transaction.batchType = it.value
                    "Payee Name" -> transaction.payeeName = it.value
                    "Check Date" -> transaction.checkDate = it.value
                    "Amount",
                    "Total Amount" -> transaction.totalAmount =
                        it.value.formatAmount(CurrencyEnum.PHP.name)
                    "Check Type" -> transaction.checkType = it.value
                }
            }.addTo(disposables)
        return transaction
    }

}

sealed class ApprovalDoneState

object ShowApprovalDoneLoading : ApprovalDoneState()

object ShowApprovalDoneEndlessLoading : ApprovalDoneState()

object ShowApprovalDoneDismissLoading : ApprovalDoneState()

object ShowApprovalDoneDismissEndlessLoading : ApprovalDoneState()

data class ShowApprovalDoneError(val throwable: Throwable) : ApprovalDoneState()