package com.unionbankph.corporate.fund_transfer.presentation.bank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BankViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway,
    private val checkDepositGateway: CheckDepositGateway
) : BaseViewModel() {

    private val _banks = MutableLiveData<MutableList<SectionedData<Bank>>>()

    val banks: LiveData<MutableList<SectionedData<Bank>>> get() = _banks

    var searchItems: MutableList<Bank> = mutableListOf()

    fun getBanks(channelId: String?) {
        val gateway = when (channelId) {
            CHANNEL_PESO_NET -> {
                fundTransferGateway.getPesoNetBanks()
            }
            CHANNEL_CHECK_DEPOSIT -> {
                checkDepositGateway.getCheckDepositBanks()
            }
            CHANNEL_INSTAPAY -> {
                fundTransferGateway.getInstaPayBanks()
            }
            CHANNEL_PDDTS -> {
                fundTransferGateway.getPDDTSBanks()
            }
            else -> {
                fundTransferGateway.getPesoNetBanks()
            }
        }
        gateway.map { list ->
            searchItems = list
            mapSectionedDate(list)
        }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _banks.value = it
                }, {
                    Timber.e(it, "getPesoNetBanks Failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    private fun mapSectionedDate(list: MutableList<Bank>): MutableList<SectionedData<Bank>> {
        val sectionedList = mutableListOf<SectionedData<Bank>>()
        val distinctBankTitle = list.asSequence()
            .sortedWith(compareBy { it.bank?.get(0) })
            .distinctBy {
                Pair(
                    it.bank?.get(0),
                    it.bank?.get(0)
                )
            }.map { it.bank }
            .toMutableList()
        distinctBankTitle
            .forEach { bank ->
                val filteredList = list
                    .filter { bank?.get(0) == it.bank?.get(0) }
                    .toMutableList()
                sectionedList.add(SectionedData(header = bank, data = filteredList))
            }
        return sectionedList
    }

    fun filterList(filter: String) {
        Single.fromCallable {
            searchItems
        }.map {
            if (filter != "") {
                val filteredList = searchItems.filter {
                    it.bank?.contains(filter, true) == true
                }.toMutableList()
                mapSectionedDate(filteredList)
            } else {
                mapSectionedDate(searchItems)
            }
        }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _banks.value = it
                }, {
                    Timber.e(it, "filterList")
                }
            ).addTo(disposables)
    }

    companion object {
        const val CHANNEL_PESO_NET = "4"
        const val CHANNEL_INSTAPAY = "7"
        const val CHANNEL_PDDTS = "1"
        const val CHANNEL_CHECK_DEPOSIT = "5"
    }

}
