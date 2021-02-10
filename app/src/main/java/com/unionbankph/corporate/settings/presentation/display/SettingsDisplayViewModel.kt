package com.unionbankph.corporate.settings.presentation.display

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.LIST_VIEW_DISPLAY
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class SettingsDisplayViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway,
    private val eventBus: EventBus
) : BaseViewModel() {

    val isTableView = BehaviorSubject.createDefault<Boolean>(false)

    init {
        isTableView()
    }

    fun isTableView() {
        settingsGateway.isTableView()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    isTableView.onNext(it)
                }, {
                    Timber.e(it, "isTableView failed")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    fun onClickedDisplay(tag: String) {
        val tableViewParam = tag == LIST_VIEW_DISPLAY
        settingsGateway.setTableView(tableViewParam)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    isTableView.onNext(tableViewParam)
                    eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_RESET_LIST_UI))
                }, {
                    Timber.e(it, "onClickedDisplay failed")
                    _uiState.value = Event(UiState.Error(it))
                }
            )
            .addTo(disposables)
    }
}
