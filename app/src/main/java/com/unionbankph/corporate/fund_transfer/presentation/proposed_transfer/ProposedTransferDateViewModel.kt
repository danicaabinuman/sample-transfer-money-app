package com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.corporate.data.model.RecurrenceTypes
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class ProposedTransferDateViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _settingsState = MutableLiveData<ProposedTransferDateState>()
    val state: LiveData<ProposedTransferDateState> get() = _settingsState

    val isEnabledButton = BehaviorSubject.createDefault(false)

    init {
        getRecurrenceTypes()
    }

    private fun getRecurrenceTypes() {
        settingsGateway.getRecurrenceTypes()
            .map {
                it.filter {
                    it.name.equals("One-time", true) ||
                            it.name.equals("Daily", true) ||
                            it.name.equals("Weekly", true) ||
                            it.name.equals("Monthly", true) ||
                            it.name.equals("Annually", true)
                }
                    .sortedWith(compareBy { recurrenceType -> recurrenceType.id })
                    .toMutableList()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _settingsState.value = ShowProposedTransferDateLoading }
            .doFinally { _settingsState.value = ShowProposedTransferDateDismissLoading }
            .subscribe(
                {
                    _settingsState.value =
                        ShowGetRecurrenceTypes(it)
                }, {
                    Timber.e(it, "getRecurrenceTypes failed")
                    _settingsState.value = ShowProposedTransferDateError(it)
                }).addTo(disposables)
    }
}

sealed class ProposedTransferDateState

object ShowProposedTransferDateLoading : ProposedTransferDateState()

object ShowProposedTransferDateDismissLoading : ProposedTransferDateState()

data class ShowGetRecurrenceTypes(
    val data: MutableList<RecurrenceTypes>
) : ProposedTransferDateState()

data class ShowProposedTransferDateError(val throwable: Throwable) : ProposedTransferDateState()
