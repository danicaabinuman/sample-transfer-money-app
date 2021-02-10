package com.unionbankph.corporate.fund_transfer.presentation.scheduled

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.ScheduledTransferDeletionForm
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class ManageScheduledTransferViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway
) : BaseViewModel() {

    val isValidReasonForCancellation = BehaviorSubject.createDefault(false)

    private val _state = MutableLiveData<ManageScheduledTransferState>()

    private val _transactions = MutableLiveData<MutableList<Transaction>>()

    private val _transactionsTestData = MutableLiveData<MutableList<Transaction>>()

    val state: LiveData<ManageScheduledTransferState> get() = _state

    val transactions: LiveData<MutableList<Transaction>> get() = _transactions

    val transactionsTestData: LiveData<MutableList<Transaction>> get() = _transactionsTestData

    private var getManageScheduledTransfersDisposable: Disposable? = null

    val selectedCount = BehaviorSubject.createDefault(0)

    fun getManageScheduledTransfers(status: String, pageable: Pageable, isInitialLoading: Boolean) {
        getManageScheduledTransfersDisposable?.dispose()
        getManageScheduledTransfersDisposable =
            fundTransferGateway.getManageScheduledTransfers(status, pageable)
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                }
                .map { pageable.combineList(transactions.value, it.results) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe {
                    _state.value =
                        if (isInitialLoading)
                            ShowManageScheduledTransferLoading
                        else
                            ShowManageScheduledTransferEndlessLoading
                }
                .doFinally {
                    _state.value =
                        if (isInitialLoading)
                            ShowManageScheduledTransferDismissLoading
                        else
                            ShowManageScheduledTransferEndlessDismissLoading
                }
                .subscribe(
                    {
                        _transactions.value = it
                    }, {
                        Timber.e(it, "getManageScheduledTransfers failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _state.value = ShowManageScheduledTransferError(it)
                        }
                    }
                )
        getManageScheduledTransfersDisposable?.addTo(disposables)
    }

    fun deleteScheduledTransfer(scheduledTransferDeletionForm: ScheduledTransferDeletionForm) {
        fundTransferGateway.deleteScheduledTransfer(scheduledTransferDeletionForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _state.value = ShowManageScheduledTransferSubmitLoading
            }
            .doFinally {
                _state.value =
                    ShowManageScheduledTransferSubmitDismissLoading
            }
            .subscribe(
                {
                    _state.value =
                        ShowManageScheduledDeleteScheduledTransfer(
                            scheduledTransferDeletionForm.batchIds.size
                        )
                }, {
                    Timber.e(it, "deleteBeneficiary Failed")
                    _state.value = ShowManageScheduledTransferError(it)
                })
            .addTo(disposables)
    }

    fun getScheduledTransferTutorialTestData() {
        fundTransferGateway.getScheduledTransferTutorialTestData()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _transactionsTestData.value = it.results
                }, {
                    Timber.e(it, "Get account failed")
                    _state.value = ShowManageScheduledTransferError(it)
                })
            .addTo(disposables)
    }

    fun selectItem(index: Int) {
        transactions.value?.let {
            it[index].hasSelected = !it[index].hasSelected
        }
        _transactions.value = transactions.value
        selectedCount.onNext(getTotalSelected())
    }

    fun selectAll() {
        transactions.value?.let {
            it.forEach {
                it.hasSelected = true
            }
        }
        _transactions.value = transactions.value
        selectedCount.onNext(getTotalSelected())
    }

    fun deSelectAll() {
        transactions.value?.let {
            it.forEach {
                it.hasSelected = false
            }
        }
        _transactions.value = transactions.value
        selectedCount.onNext(getTotalSelected())
    }

    private fun getTotalSelected(): Int {
        transactions.value?.let {
            return Observable.fromIterable(it)
                .filter { it.hasSelected }
                .count()
                .blockingGet().toInt()
        }
        return 0
    }

    companion object {
        const val STATUS_ONGOING = "ACTIVE"
        const val STATUS_DONE = "INACTIVE"
    }
}

sealed class ManageScheduledTransferState

object ShowManageScheduledTransferSubmitLoading : ManageScheduledTransferState()

object ShowManageScheduledTransferLoading : ManageScheduledTransferState()

object ShowManageScheduledTransferEndlessLoading : ManageScheduledTransferState()

object ShowManageScheduledTransferDismissLoading : ManageScheduledTransferState()

object ShowManageScheduledTransferSubmitDismissLoading : ManageScheduledTransferState()

object ShowManageScheduledTransferEndlessDismissLoading : ManageScheduledTransferState()

data class ShowManageScheduledDeleteScheduledTransfer(val size: Int) :
    ManageScheduledTransferState()

data class ShowManageScheduledTransferError(val throwable: Throwable) :
    ManageScheduledTransferState()
