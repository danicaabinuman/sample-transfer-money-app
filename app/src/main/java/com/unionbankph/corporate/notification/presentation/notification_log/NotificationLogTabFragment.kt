package com.unionbankph.corporate.notification.presentation.notification_log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.setEnableView
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.NotificationSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentTabNotificationsBinding
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.presentation.notification_log.notification_log_detail.NotificationLogDetailFragment
import com.unionbankph.corporate.notification.presentation.notification_log.notification_log_list.NotificationLogFragment
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class NotificationLogTabFragment :
    BaseFragment<FragmentTabNotificationsBinding, NotificationLogViewModel>() {

    private val imageViewMarkAllAsRead by lazyFast {
        (activity as DashboardActivity).imageViewMarkAllAsRead()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_LOAD_NOTIFICATION_LOG_LIST)
            )
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_ALERTS)
            )
            imageViewMarkAllAsRead.isVisible = true
            imageViewMarkAllAsRead.setEnableView(false)
        }
    }

    private fun initListener() {
        initEventBus()
    }

    private fun init() {
        navigator.addFragmentWithAnimation(
            R.id.frameLayoutNotifications,
            NotificationLogFragment(),
            null,
            childFragmentManager,
            FRAGMENT_NOTIFICATION_LOG
        )
    }

    private fun initEventBus() {
        eventBus.notificationSyncEvent.flowable.subscribe {
            when (it.eventType) {
                NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_ALERTS -> {
                    hasInitialLoad = true
                }
            }
        }.addTo(disposables)
        eventBus.fragmentSettingsSyncEvent.flowable
            .subscribe(
                {
                    when (it.eventType) {
                        FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION_LOG_ITEM -> {
                            val data = JsonHelper.fromJson<NotificationLogDto>(it.payload)
                            val bundle = Bundle()
                            bundle.putLong(
                                NotificationLogDetailFragment.EXTRA_ID,
                                data.id ?: 0L
                            )
                            bundle.putBoolean(
                                NotificationLogDetailFragment.EXTRA_IS_READ,
                                data.isRead
                            )
                            navigator.addFragmentWithAnimation(
                                R.id.frameLayoutNotifications,
                                NotificationLogDetailFragment(),
                                bundle,
                                childFragmentManager,
                                FRAGMENT_NOTIFICATION_LOG_DETAIL
                            )
                        }
                    }
                }, {
                    Timber.d(it, "fragmentSettingsSyncEvent")
                }
            ).addTo(disposables)
    }

    companion object {
        const val FRAGMENT_NOTIFICATION_LOG = "notification_log"
        const val FRAGMENT_NOTIFICATION_LOG_DETAIL = "notification_log_detail"
    }

    override val viewModelClassType: Class<NotificationLogViewModel>
        get() = NotificationLogViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTabNotificationsBinding
        get() = FragmentTabNotificationsBinding::inflate
}
