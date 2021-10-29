package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Completable
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

    val settingsState: LiveData<SettingsState> get() = _settingsState

    private val _navigateToLocalSettings = MutableLiveData<Event<SettingsState>>()

    val navigateToLocalSettings: LiveData<Event<SettingsState>> get() = _navigateToLocalSettings

    fun saveSettings(isChecked: Boolean, isCheckedTOTP: Boolean) {
        notificationGateway.getNotifications()
            .flatMap {
                notificationGateway.updateNotificationSettings(
                    NotificationForm(it.notifications.sortedBy { it.notificationId }.toMutableList(),
                    isChecked))
            }
            .flatMapCompletable {
                if (isCheckedTOTP) {
                    settingsGateway.totpSubscribe(ManageDeviceForm(""))
                } else {
                    Completable.complete()
                }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    Timber.d("Success Settings!")
                    _navigateToLocalSettings.value = Event(SetSettingsSuccess)
                }, {
                    Timber.e(it, "saveSettings Failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    fun considerAsRecentUser(promptTypeEnum: PromptTypeEnum) {
        settingsGateway.considerAsRecentUser(promptTypeEnum)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    Timber.d("considerAsRecentUser")
                }, {
                    Timber.e(it, "considerAsRecentUser failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }
    sealed class SettingsState
    object SetSettingsSuccess : SettingsState()

}