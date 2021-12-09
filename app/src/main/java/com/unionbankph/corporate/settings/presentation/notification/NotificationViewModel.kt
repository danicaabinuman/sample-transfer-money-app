package com.unionbankph.corporate.settings.presentation.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.ResultSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val notificationGateway: NotificationGateway,
    private val eventBus: EventBus
) : BaseViewModel() {

    private val _notificationState = MutableLiveData<NotificationState>()

    val state: LiveData<NotificationState> get() = _notificationState

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
            .doOnSubscribe {
                _notificationState.value =
                    ShowNotificationLoading
            }
            .doFinally {
                _notificationState.value =
                    ShowNotificationDismissLoading
            }
            .subscribe(
                {
                    _notificationState.value =
                        ShowNotificationData(
                            it
                        )
                }, {
                    Timber.e(it, "getNotifications Failed")
                    _notificationState.value =
                        ShowNotificationError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun updateNotificationSettings(notificationForm: NotificationForm) {
        notificationGateway.updateNotificationSettings(notificationForm)
            .map {
                NotificationDto(
                   it.notifications.sortedBy { it.notificationId }.toMutableList(),
                    it.receiveAllNotifications
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _notificationState.value =
                    ShowNotificationLoadingSubmit
            }
            .doFinally {
                _notificationState.value =
                    ShowNotificationDismissLoadingSubmit
            }
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
    }

    fun getNotification(id: String) {
        notificationGateway.getNotification(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _notificationState.value =
                    ShowNotificationLoading
            }
            .doOnSuccess {
                _notificationState.value =
                    ShowNotificationDismissLoading
            }
            .subscribe(
                {
                    _notificationState.value =
                        ShowNotificationDetailData(
                            it
                        )
                }, {
                    Timber.e(it, "getNotifications Failed")
                    _notificationState.value =
                        ShowNotificationError(
                            it
                        )
                })
            .addTo(disposables)
    }
}

sealed class NotificationState

object ShowNotificationLoading : NotificationState()
object ShowNotificationLoadingSubmit : NotificationState()
object ShowNotificationDismissLoading : NotificationState()
object ShowNotificationDismissLoadingSubmit : NotificationState()
data class ShowNotificationData(val data: NotificationDto) : NotificationState()
data class ShowNotificationDetailData(val data: Notification) : NotificationState()
data class ShowNotificationError(val throwable: Throwable) : NotificationState()
