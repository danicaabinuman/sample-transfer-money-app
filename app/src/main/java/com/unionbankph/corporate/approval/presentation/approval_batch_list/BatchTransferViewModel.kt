package com.unionbankph.corporate.approval.presentation.approval_batch_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.Batch
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject


class BatchTransferViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway
) : BaseViewModel() {

    private val _batchTransferState = MutableLiveData<BatchTransferState>()
    private val batchTransferMutableLiveDate = MutableLiveData<PagedDto<Batch>>()

    val state: LiveData<BatchTransferState> get() = _batchTransferState
    val batchTransferLiveDate: LiveData<PagedDto<Batch>> get() = batchTransferMutableLiveDate

    fun getBatchTransaction(pageable: Pageable, id: String?, isInitialLoading: Boolean) {
        fundTransferGateway.getBatchTransaction(pageable, id.notNullable())
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _batchTransferState.value =
                    if (isInitialLoading)
                        ShowBatchTransferLoading
                    else
                        ShowBatchTransferEndlessLoading
            }
            .doFinally {
                _batchTransferState.value =
                    if (isInitialLoading)
                        ShowBatchTransferDismissLoading
                    else
                        ShowBatchTransferEndlessDismissLoading
            }
            .subscribe(
                {
                    batchTransferMutableLiveDate.value = it
                }, {
                    Timber.e(it, "getBatchTransaction Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _batchTransferState.value = ShowBatchTransferError(it)
                    }
                }
            ).addTo(disposables)
    }

}

sealed class BatchTransferState

object ShowBatchTransferLoading : BatchTransferState()

object ShowBatchTransferDismissLoading : BatchTransferState()

object ShowBatchTransferEndlessLoading : BatchTransferState()

object ShowBatchTransferEndlessDismissLoading : BatchTransferState()

data class ShowBatchTransferError(val throwable: Throwable) : BatchTransferState()
