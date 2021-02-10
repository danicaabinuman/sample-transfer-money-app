package com.unionbankph.corporate.approval.presentation.approval_activity_log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.domain.interactor.GetCheckWriterActivityLogs
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import io.reactivex.Completable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject


class ActivityLogViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val viewUtil: ViewUtil,
    private val fundTransferGateway: FundTransferGateway,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val getCheckWriterActivityLogs: GetCheckWriterActivityLogs
) : BaseViewModel() {

    private val _activityLogState = MutableLiveData<ActivityLogState>()

    val state: LiveData<ActivityLogState> get() = _activityLogState

    var activityLogsHeader: MutableList<ActivityLogDto> = mutableListOf()

    var activityLogs: MutableList<ActivityLogDto> = mutableListOf()

    fun getFundTransferActivityLogs(id: String) {
        fundTransferGateway.getFundTransferActivityLogs(id)
            .doOnSuccess { activityLogs = it }
            .map { list ->
                activityLogsHeader = list.asSequence().distinctBy {
                    Pair(
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        ),
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        )
                    )
                }.toMutableList()
                Completable.complete()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _activityLogState.value =
                    ShowActivityLogLoading
            }
            .doFinally {
                _activityLogState.value =
                    ShowActivityLogDismissLoading
            }
            .subscribe(
                {
                    _activityLogState.value =
                        ShowActivityLogSuccess
                }, {
                    Timber.e(it, "getBillsPaymentActivityLogs Failed")
                    _activityLogState.value =
                        ShowActivityLogError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getBillsPaymentActivityLogs(id: String) {
        billsPaymentGateway.getBillsPaymentActivityLogs(id)
            .doOnSuccess { activityLogs = it }
            .map { list ->
                activityLogsHeader = list.asSequence().distinctBy {
                    Pair(
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        ),
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        )
                    )
                }.toMutableList()
                Completable.complete()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _activityLogState.value =
                    ShowActivityLogLoading
            }
            .doFinally {
                _activityLogState.value =
                    ShowActivityLogDismissLoading
            }
            .subscribe(
                {
                    _activityLogState.value =
                        ShowActivityLogSuccess
                }, {
                    Timber.e(it, "getBillsPaymentActivityLogs Failed")
                    _activityLogState.value =
                        ShowActivityLogError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getCheckWriterActivityLogs(id: String) {
        getCheckWriterActivityLogs.execute(
            getDisposableSingleObserver(
                {
                    activityLogsHeader = it.first
                    activityLogs = it.second
                    _activityLogState.value = ShowActivityLogSuccess
                }, {
                    Timber.e(it, "getCheckWriterActivityLogs")
                    _activityLogState.value = ShowActivityLogError(it)
                }
            ),
            doOnSubscribeEvent = {
                _activityLogState.value = ShowActivityLogLoading
            },
            doFinallyEvent = {
                _activityLogState.value = ShowActivityLogDismissLoading
            },
            params = id
        ).addTo(disposables)
    }

    fun getBeneficiaryDetailActivityLogs(id: String?) {
        fundTransferGateway.getBeneficiaryDetailActivityLogs(id.notNullable())
            .doOnSuccess { activityLogs = it }
            .map { list ->
                activityLogsHeader = list.asSequence().distinctBy {
                    Pair(
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        ),
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        )
                    )
                }.toMutableList()
                Completable.complete()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _activityLogState.value =
                    ShowActivityLogLoading
            }
            .doFinally {
                _activityLogState.value =
                    ShowActivityLogDismissLoading
            }
            .subscribe(
                {
                    _activityLogState.value =
                        ShowActivityLogSuccess
                }, {
                    Timber.e(it, "getBeneficiaryDetailActivityLogs Failed")
                    _activityLogState.value =
                        ShowActivityLogError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getFrequentBillerActivityLogs(id: String?) {
        billsPaymentGateway.getFrequentBillerActivityLogs(id.notNullable())
            .doOnSuccess { activityLogs = it }
            .map { list ->
                activityLogsHeader = list.asSequence().distinctBy {
                    Pair(
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        ),
                        viewUtil.getDateFormatByDateString(
                            it.createdDate,
                            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                            ViewUtil.DATE_FORMAT_WITHOUT_TIME
                        )
                    )
                }.toMutableList()
                Completable.complete()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _activityLogState.value =
                    ShowActivityLogLoading
            }
            .doFinally {
                _activityLogState.value =
                    ShowActivityLogDismissLoading
            }
            .subscribe(
                {
                    _activityLogState.value =
                        ShowActivityLogSuccess
                }, {
                    Timber.e(it, "getFrequentBillerActivityLogs Failed")
                    _activityLogState.value =
                        ShowActivityLogError(
                            it
                        )
                })
            .addTo(disposables)
    }

}

sealed class ActivityLogState

object ShowActivityLogLoading : ActivityLogState()

object ShowActivityLogDismissLoading : ActivityLogState()

object ShowActivityLogSuccess : ActivityLogState()

data class ShowActivityLogError(val throwable: Throwable) : ActivityLogState()

