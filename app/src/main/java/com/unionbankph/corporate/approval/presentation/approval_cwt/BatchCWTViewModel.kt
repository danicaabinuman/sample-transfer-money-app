package com.unionbankph.corporate.approval.presentation.approval_cwt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.BATCH_TYPE_CWT
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.CWTDetail
import com.unionbankph.corporate.fund_transfer.data.model.CWTItem
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject


class BatchCWTViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway
) : BaseViewModel() {

    private val batchStateMutableLiveData = MutableLiveData<BatchCWTState>()
    private val cwtHeaderMutableLiveData = MutableLiveData<MutableList<CWTDetail>>()
    private val cwtMutableLiveData = MutableLiveData<MutableList<CWTItem>>()
    val batchStateLiveData: LiveData<BatchCWTState> = batchStateMutableLiveData
    val cwtHeaderLiveData: LiveData<MutableList<CWTDetail>> = cwtHeaderMutableLiveData
    val cwtLiveData: LiveData<MutableList<CWTItem>> = cwtMutableLiveData

    fun getFundTransferCWTHeader(type: String) {
        (if (type == BATCH_TYPE_CWT)
            fundTransferGateway.getFundTransferCWTHeader()
        else
            fundTransferGateway.getFundTransferINVHeader())
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                batchStateMutableLiveData.value =
                    ShowBatchCWTDetailLoading
            }
            .doFinally {
                batchStateMutableLiveData.value =
                    ShowBatchCWTDetailDismissLoading
            }
            .subscribe(
                {
                    cwtHeaderMutableLiveData.value = it
                }, {
                    Timber.e(it, "getBatchTransaction Failed")
                    batchStateMutableLiveData.value =
                        ShowBatchDetailCWTError(
                            it
                        )
                }
            ).addTo(disposables)
    }

    fun getFundTransferCWT(
        type: String,
        pageable: Pageable,
        id: String,
        isInitialLoading: Boolean
    ) {
        (if (type == BATCH_TYPE_CWT)
            fundTransferGateway.getFundTransferCWT(pageable, id)
        else
            fundTransferGateway.getFundTransferINV(pageable, id))
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    isInitialLoad = isInitialLoading
                    increasePage()
                }
            }
            .flatMapObservable { Observable.fromIterable(it.results) }
            .map {
                CWTItem().apply {
                    title = if (type == BATCH_TYPE_CWT)
                        "Creditable Withholding Tax Line"
                    else
                        "Additional Information Line"
                    cwtBody = it
                }
            }
            .toList()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                batchStateMutableLiveData.value =
                    if (isInitialLoading)
                        ShowBatchCWTLoading
                    else
                        ShowBatchCWTEndlessLoading
            }
            .doFinally {
                batchStateMutableLiveData.value =
                    if (isInitialLoading)
                        ShowBatchCWTDismissLoading
                    else
                        ShowBatchCWTDismissEndlessLoading
            }
            .subscribe(
                {
                    cwtMutableLiveData.value = it
                }, {
                    Timber.e(it, "getFundTransferCWT Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        batchStateMutableLiveData.value = ShowBatchCWTError(it)
                    }
                }
            ).addTo(disposables)
    }

}

sealed class BatchCWTState

object ShowBatchCWTLoading : BatchCWTState()

object ShowBatchCWTDetailLoading : BatchCWTState()

object ShowBatchCWTEndlessLoading : BatchCWTState()

object ShowBatchCWTDismissLoading : BatchCWTState()

object ShowBatchCWTDetailDismissLoading : BatchCWTState()

object ShowBatchCWTDismissEndlessLoading : BatchCWTState()

data class ShowBatchCWTError(val throwable: Throwable) : BatchCWTState()

data class ShowBatchDetailCWTError(val throwable: Throwable) : BatchCWTState()
