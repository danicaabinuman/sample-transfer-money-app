package com.unionbankph.corporate.mcd.presentation.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.convertToCalendar
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.SectionedCheckDepositLogs
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class CheckDepositActivityLogViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val checkDepositGateway: CheckDepositGateway,
    private val viewUtil: ViewUtil
) : BaseViewModel() {

    private val _checkDepositLogState = MutableLiveData<CheckDepositActivityLogState>()

    private val checkDepositLogsMutableLiveData =
        MutableLiveData<MutableList<SectionedCheckDepositLogs>>()

    val state: LiveData<CheckDepositActivityLogState> get() = _checkDepositLogState

    val sectionedCheckDepositLogsState: LiveData<MutableList<SectionedCheckDepositLogs>>
        get() =
            checkDepositLogsMutableLiveData

    fun getCheckDepositActivityLogs(id: String) {
        checkDepositGateway.getCheckDepositActivityLogs(id)
            .map { checkDepositActivityLogs ->
                val sectionedCheckDepositLogs = mutableListOf<SectionedCheckDepositLogs>()
                val headers =
                    checkDepositActivityLogs.asSequence()
                        .sortedBy { it.createdDate.convertToCalendar() }
                        .distinctBy {
                            Pair(
                                convertStringDate(it.createdDate),
                                convertStringDate(it.createdDate)
                            )
                        }
                        .toMutableList()
                headers.forEach { header ->
                    val headerConvertedDate = convertStringDate(header.createdDate)
                    val filteredSectionedCheckDepositLogs = checkDepositActivityLogs
                        .sortedBy { it.createdDate.convertToCalendar() }
                        .filter {
                            convertStringDate(it.createdDate) == headerConvertedDate
                        }
                        .toMutableList()
                    sectionedCheckDepositLogs.add(
                        SectionedCheckDepositLogs(
                            headerConvertedDate,
                            filteredSectionedCheckDepositLogs
                        )
                    )
                }
                return@map sectionedCheckDepositLogs
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _checkDepositLogState.value = ShowCheckDepositLogLoading }
            .doFinally { _checkDepositLogState.value = ShowCheckDepositLogDismissLoading }
            .subscribe(
                {
                    checkDepositLogsMutableLiveData.value = it
                }, {
                    Timber.e(it, "getFrequentBillerActivityLogs Failed")
                    _checkDepositLogState.value = ShowCheckDepositLogError(it)
                })
            .addTo(disposables)
    }

    private fun convertStringDate(createdDate: String?): String {
        return viewUtil.getDateFormatByDateString(
            createdDate,
            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
            ViewUtil.DATE_FORMAT_WITHOUT_TIME
        )
    }
}

sealed class CheckDepositActivityLogState

object ShowCheckDepositLogLoading : CheckDepositActivityLogState()

object ShowCheckDepositLogDismissLoading : CheckDepositActivityLogState()

data class ShowCheckDepositLogError(val throwable: Throwable) : CheckDepositActivityLogState()
