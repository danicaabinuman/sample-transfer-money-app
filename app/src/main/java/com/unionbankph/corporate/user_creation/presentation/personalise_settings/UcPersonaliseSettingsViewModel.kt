package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.bus.event.ResultSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.app.dashboard.Error
import com.unionbankph.corporate.app.dashboard.ShowDismissProgressLoading
import com.unionbankph.corporate.app.dashboard.ShowProgressLoading
import com.unionbankph.corporate.app.dashboard.ShowTOTPSubscription
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.notification.data.model.NotificationDto
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.notification.*
import com.unionbankph.corporate.user_creation.data.form.ValidateContactInfoForm
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class UcPersonaliseSettingsViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway,
    private val notificationGateway: NotificationGateway
) :
    BaseViewModel() {

    private val _settingsState = MutableLiveData<SettingsState>()

    val state: LiveData<SettingsState> get() = _settingsState

    fun totpSubscribe(manageDeviceForm: ManageDeviceForm) {
        settingsGateway.totpSubscribe(manageDeviceForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading)  }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _settingsState.value = ShowTOTPSubscription
                }, {
                    Timber.e(it, "totpSubscribe failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    fun getNotifications() {
        notificationGateway.getNotifications()
            .map {
                NotificationDto(
                    it.notifications.sortedBy { it.notificationId }.toMutableList(),
                    it.receiveAllNotifications
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                { _settingsState.value = ShowNotificationData(it)
                }, {
                    Timber.e(it, "getNotifications Failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    /*fun updateNotificationSettings(notificationForm: NotificationForm) {
        notificationGateway.updateNotificationSettings(notificationForm)
            .map {
                NotificationDto(
                    it.notifications.sortedBy { it.notificationId }.toMutableList(),
                    it.receiveAllNotifications
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading)
            }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    if (notificationForm.notifications.size == 1) {
                        eventBus.resultSyncEvent.emmit(
                            BaseEvent(
                                ResultSyncEvent.ACTION_UPDATE_NOTIFICATION,
                                JsonHelper.toJson(it)
                            )
                        )
                    }
                    _notificationState.value =
                        ShowNotificationData(
                            it
                        )
                }, {
                    Timber.e(it, "updateNotificationSettings Failed")
                    _notificationState.value =
                        ShowNotificationError(
                            it
                        )
                })
            .addTo(disposables)
    }*/

    sealed class SettingsState
    object ShowTOTPSubscription : SettingsState()
    data class ShowNotificationData(val data: NotificationDto) : SettingsState()

}