package com.unionbankph.corporate.notification.presentation.notification_log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class NotificationLogViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val notificationGateway: NotificationGateway
) : BaseViewModel() {

    private val _notificationLogState = MutableLiveData<NotificationLogState>()

    private val _notifications = MutableLiveData<MutableList<StateData<NotificationLogDto>>>()

    val state: LiveData<NotificationLogState> get() = _notificationLogState

    val notifications: LiveData<MutableList<StateData<NotificationLogDto>>> get() = _notifications

    fun getNotificationLogs(pageable: Pageable, isInitialLoading: Boolean) {
        notificationGateway.getNotificationLogs(pageable)
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    increasePage()
                }
            }
            .toObservable()
            .flatMapIterable { it.results }
            .map { StateData(it, it.isRead) }
            .toList()
            .map { pageable.combineList(_notifications.value, it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _notificationLogState.value =
                    if (isInitialLoading) ShowLoading else ShowEndlessLoading
            }
            .doFinally {
                _notificationLogState.value =
                    if (isInitialLoading) ShowDismissLoading else ShowEndlessDismissLoading
            }
            .subscribe(
                {
                    _notifications.value = it
                }, {
                    Timber.e(it, "getNotificationLogs Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _notificationLogState.value = NotificationLogError(it)
                    }
                })
            .addTo(disposables)
    }

    fun getNotificationLogDetail(notificationId: Long) {
        notificationGateway.getNotificationLogDetail(notificationId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _notificationLogState.value = ShowLoading }
            .doFinally { _notificationLogState.value = ShowDismissLoading }
            .subscribe(
                {
                    _notificationLogState.value = GetNotificationLogDetail(it)
                }, {
                    Timber.e(it, "getNotificationLogDetail Failed")
                    _notificationLogState.value = NotificationLogError(it)
                })
            .addTo(disposables)
    }

    fun markAllAsReadNotificationLogs() {
        notificationGateway.markAllAsReadNotificationLogs()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _notificationLogState.value = ShowProgressBarLoading }
            .doFinally { _notificationLogState.value = ShowProgressBarDismissLoading }
            .subscribe(
                {
                    _notificationLogState.value = ShowMarkAllAsRead(it.notificationLogBadgeCount)
                }, {
                    Timber.e(it, "markAllAsReadNotificationLogs Failed")
                    _notificationLogState.value = NotificationLogError(it)
                })
            .addTo(disposables)
    }
}

sealed class NotificationLogState

object ShowEndlessLoading : NotificationLogState()

object ShowEndlessDismissLoading : NotificationLogState()

object ShowLoading : NotificationLogState()

object ShowDismissLoading : NotificationLogState()

object ShowProgressBarLoading : NotificationLogState()

object ShowProgressBarDismissLoading : NotificationLogState()

data class ShowMarkAllAsRead(val notificationLogBadgeCount: Int) : NotificationLogState()

data class GetNotificationLogDetail(val data: NotificationLogDto) : NotificationLogState()

data class NotificationLogError(val throwable: Throwable) : NotificationLogState()
