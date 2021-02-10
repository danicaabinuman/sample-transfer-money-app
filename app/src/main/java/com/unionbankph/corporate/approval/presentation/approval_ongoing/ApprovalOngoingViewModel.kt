package com.unionbankph.corporate.approval.presentation.approval_ongoing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatAmount
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.approval.data.model.ApprovalForm
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.CurrencyEnum
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject


class ApprovalOngoingViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val approvalGateway: ApprovalGateway,
    private val dataBus: DataBus
) : BaseViewModel() {

    val isValidReasonForRejection = BehaviorSubject.createDefault(false)

    private val _approvalState = MutableLiveData<ApprovalOngoingState>()

    private val _transactions = MutableLiveData<MutableList<Transaction>>()

    val state: LiveData<ApprovalOngoingState> get() = _approvalState

    val transactions: LiveData<MutableList<Transaction>> = _transactions


    private var getApprovalListDisposable: Disposable? = null

    fun getApprovalList(
        status: String,
        pageable: Pageable,
        isInitialLoading: Boolean
    ) {
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
                            ShowApprovalOngoingLoading
                        else
                            ShowApprovalOngoingEndlessLoading
                }
                .doFinally {
                    _approvalState.value =
                        if (isInitialLoading)
                            ShowApprovalOngoingDismissLoading
                        else
                            ShowApprovalOngoingEndlessDismissLoading
                }
                .subscribe(
                    {
                        _transactions.value = it
                    }, {
                        Timber.e(it, "Get account failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _approvalState.value = ShowApprovalOngoingError(it)
                        }
                    })
        getApprovalListDisposable?.addTo(disposables)
    }

    private fun mapTransactionDetails(transaction: Transaction): Transaction {
        transaction.id = transaction.transactionReferenceNumber
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

    fun approval(
        approvalType: String,
        reasonForRejection: String? = null,
        batchIds: MutableList<String>,
        checkWriterBatchIds: MutableList<String>
    ) {
        approvalGateway.approval(
            ApprovalForm(approvalType, reasonForRejection, batchIds, checkWriterBatchIds)
        )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _approvalState.value = ShowApprovalOngoingRequestLoading }
            .doFinally { _approvalState.value = ShowApprovalOngoingRequestDismissLoading }
            .subscribe(
                {
                    dataBus.approvalBatchIdDataBus.emmit(it!!)
                }, {
                    Timber.e(it, "Approval failed")
                    _approvalState.value = ShowApprovalOngoingError(it)
                })
            .addTo(disposables)
    }


}

sealed class ApprovalOngoingState

object ShowApprovalOngoingLoading : ApprovalOngoingState()

object ShowApprovalOngoingEndlessLoading : ApprovalOngoingState()

object ShowApprovalOngoingDismissLoading : ApprovalOngoingState()

object ShowApprovalOngoingEndlessDismissLoading : ApprovalOngoingState()

object ShowApprovalOngoingRequestLoading : ApprovalOngoingState()

object ShowApprovalOngoingRequestDismissLoading : ApprovalOngoingState()

data class ShowApprovalOngoingError(val throwable: Throwable) : ApprovalOngoingState()
