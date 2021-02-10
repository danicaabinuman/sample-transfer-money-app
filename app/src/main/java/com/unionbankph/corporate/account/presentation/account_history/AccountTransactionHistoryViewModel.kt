package com.unionbankph.corporate.account.presentation.account_history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.model.SectionedAccountTransactionHistory
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class AccountTransactionHistoryViewModel @Inject constructor(
    private val accountGateway: AccountGateway
) : BaseViewModel() {

    private val _sectionedTransactions =
        MutableLiveData<MutableList<SectionedAccountTransactionHistory>>()

    val sectionedTransactions: LiveData<MutableList<SectionedAccountTransactionHistory>> get() = _sectionedTransactions

    val lastItemRecord = BehaviorSubject.create<Record>()

    val pageable = Pageable()

    fun getRecentTransactions(id: String, isInitialLoading: Boolean) {
        val lastItemRecordItem = if (this.lastItemRecord.value?.amount == null) {
            null
        } else {
            lastItemRecord.value
        }
        accountGateway.getRecentTransactions(id.notNullable(), pageable, lastItemRecordItem)
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = (it.totalRecords?.toDouble())?.roundToInt() ?: 0
                    isLastPage = it.nextPage == null
                    increasePage()
                }
            }
            .map {
                lastItemRecord.onNext(it.records.last())
                return@map mapRecords(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
                    _sectionedTransactions.value = it
                }, {
                    Timber.e(it, "getRecentTransactions failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _uiState.value = Event(UiState.Error(it))
                    }
                })
            .addTo(disposables)
    }

    private fun mapRecords(it: RecentTransaction): MutableList<SectionedAccountTransactionHistory>? {
        if (pageable.isInitialLoad) {
            sectionedTransactions.value?.clear()
        }
        val previousHeaderRecords = this.sectionedTransactions.value?.map {
            it.header.notNullable()
        }?.toMutableList()

        val newHeaderRecords = it.records.distinctBy {
            Pair(getDate(it.postedDate), getDate(it.postedDate))
        }.map {
            getDate(it.postedDate)
        }.toMutableList()

        if (!sectionedTransactions.value.isNullOrEmpty() &&
            previousHeaderRecords?.last() != null &&
            newHeaderRecords.contains(previousHeaderRecords.last())
        ) {
            val relatedInLastRecords = it.records
                .filter { previousHeaderRecords.last() == getDate(it.postedDate) }
            this.sectionedTransactions.value?.last()?.records?.addAll(relatedInLastRecords)

            newHeaderRecords
                .filter { previousHeaderRecords.last() != it }
                .forEach { header ->
                    val filteredRecords = it.records
                        .filter {
                            previousHeaderRecords.last() != getDate(it.postedDate) &&
                                    header == getDate(it.postedDate)
                        }.toMutableList()
                    sectionedTransactions.value?.add(
                        SectionedAccountTransactionHistory(header, filteredRecords)
                    )
                }
            return sectionedTransactions.value
        } else {
            if (!sectionedTransactions.value.isNullOrEmpty()) {
                newHeaderRecords
                    .forEach { header ->
                        val filteredList = it.records
                            .filter { getDate(it.postedDate) == header }
                            .toMutableList()
                        sectionedTransactions.value?.add(
                            SectionedAccountTransactionHistory(header, filteredList)
                        )
                    }
                return sectionedTransactions.value
            } else {
                val newList =
                    mutableListOf<SectionedAccountTransactionHistory>()
                newHeaderRecords
                    .forEach { header ->
                        val filteredList = it.records
                            .filter { getDate(it.postedDate) == header }
                            .toMutableList()
                        newList.add(
                            SectionedAccountTransactionHistory(header, filteredList)
                        )
                    }
                return newList
            }
        }
    }

    private fun getDate(postedDate: String?): String {
        return postedDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE).notNullable()
    }
}
