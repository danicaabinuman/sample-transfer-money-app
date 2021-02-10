package com.unionbankph.corporate.general.presentation.transaction_filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.isEmpty
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.corporate.domain.interactor.GetTransactionStatuses
import com.unionbankph.corporate.general.data.model.TransactionFilterForm
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TransactionFilterViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val viewUtil: ViewUtil,
    private val eventBus: EventBus,
    private val corporateGateway: CorporateGateway,
    private val getTransactionStatuses: GetTransactionStatuses
) : BaseViewModel() {

    private val _uiStateChannels = MutableLiveData<UiState>()

    val uiStateChannels: LiveData<UiState> get() = _uiStateChannels

    private val _uiStateStatuses = MutableLiveData<UiState>()

    val uiStateStatuses: LiveData<UiState> get() = _uiStateStatuses

    private val _channels = MutableLiveData<MutableList<Selector>>()

    val channels: LiveData<MutableList<Selector>> get() = _channels

    private val _statuses = MutableLiveData<MutableList<Selector>>()

    val statuses: LiveData<MutableList<Selector>> get() = _statuses

    val screen = BehaviorSubject.createDefault("")

    val channelsInput = BehaviorSubject.createDefault("")
    val channelsToDisplayInput = BehaviorSubject.createDefault("")
    val channelsStateDataInput = BehaviorSubject.create<MutableList<StateData<Selector>>>()

    val statusInput = BehaviorSubject.createDefault("")
    val statusToDisplayInput = BehaviorSubject.createDefault("")
    val statusesStateDataInput = BehaviorSubject.create<MutableList<StateData<Selector>>>()

    val billerInput = BehaviorSubject.create<Biller>()

    val startDateInput = BehaviorSubject.create<Calendar>()
    val endDateInput = BehaviorSubject.create<Calendar>()

    fun fillStartDateInput(selectedDate: Calendar) {
        if (endDateInput.hasValue()) {
            endDateInput.value?.let {
                if (selectedDate.timeInMillis > it.timeInMillis) {
                    endDateInput.onNext(selectedDate)
                }
            }
        }
        startDateInput.onNext(selectedDate)
    }

    fun onClickedApplyFilter() {
        if (getFilterCount() > 0) {
            val transactionFilterForm = TransactionFilterForm().apply {
                startDateInput.value?.let {
                    startDate = viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                    )
                }
                endDateInput.value?.let {
                    endDate = viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                    )
                }
                channels = channelsInput.value
                statuses = statusInput.value
                channelsStateData = channelsStateDataInput.value
                statusesStateData = statusesStateDataInput.value
                channelsValueToDisplay = channelsToDisplayInput.value
                statusesValueToDisplay = statusToDisplayInput.value
                biller = billerInput.value
                count = getFilterCount()
            }
            eventBus.actionSyncEvent.emmit(
                BaseEvent(
                    ActionSyncEvent.ACTION_APPLY_FILTER_FUND_TRANSFER,
                    JsonHelper.toJson(transactionFilterForm)
                )
            )
        } else {
            eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_CLEAR_FILTER))
        }
        _uiState.value = Event(UiState.Complete)
    }

    private fun getFilterCount(): Long {
        var count = 0L
        if (!channelsInput.value.isEmpty() && screen.value == TransactionFilterActivity.FUND_TRANSFER_SCREEN) {
            count++
        }
        if (!statusInput.value.isEmpty()) {
            count++
        }
        if (startDateInput.hasValue() || endDateInput.hasValue()) {
            count++
        }
        if (billerInput.hasValue() && screen.value == TransactionFilterActivity.BILLS_PAYMENT_SCREEN) {
            count++
        }
        return count
    }

    fun onClickedClearFilter() {
        eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_CLEAR_FILTER))
        _uiState.value = Event(UiState.Complete)
    }

    fun onClickedChannels() {
        if (channels.value.notNullable().isNotEmpty()) {
            _channels.value = channels.value
        } else {
            corporateGateway.getChannelsLite("2")
                .toObservable()
                .flatMapIterable { it }
                .map {
                    Selector(
                        it.id.toString(),
                        it.id.toString(),
                        it.name
                    )
                }
                .toList()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe { _uiStateChannels.value = UiState.Loading }
                .doFinally { _uiStateChannels.value = UiState.Complete }
                .subscribe(
                    {
                        _channels.value = it
                    }, {
                        Timber.e(it, "onClickedChannels")
                        _uiStateChannels.value = UiState.Error(it)
                    }
                ).addTo(disposables)
        }
    }

    fun onClickedStatuses() {
        if (statuses.value.notNullable().isNotEmpty()) {
            _statuses.value = statuses.value
        } else {
            getTransactionStatuses.execute(
                getDisposableSingleObserver(
                    {
                        _statuses.value = it
                    }, {
                        Timber.e(it, "onClickedStatuses")
                        _uiStateStatuses.value = UiState.Error(it)
                    }
                ),
                doOnSubscribeEvent = {
                    _uiStateStatuses.value = UiState.Loading
                },
                doFinallyEvent = {
                    _uiStateStatuses.value = UiState.Complete
                },
                params = "2"
            ).addTo(disposables)
        }
    }

}
