package com.unionbankph.corporate.notification.presentation.notification_log.notification_log_detail

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.presentation.notification_log.GetNotificationLogDetail
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogError
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogViewModel
import com.unionbankph.corporate.notification.presentation.notification_log.ShowDismissLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowLoading
import kotlinx.android.synthetic.main.fragment_notification_log_detail.*

class NotificationLogDetailFragment :
    BaseFragment<NotificationLogViewModel>(R.layout.fragment_notification_log_detail) {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[NotificationLogViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowLoading -> {
                    showLoading()
                }
                is ShowDismissLoading -> {
                    dismissLoading()
                }
                is GetNotificationLogDetail -> {
                    initNotificationLogDetail(it.data)
                }
                is NotificationLogError -> {
                    scrollViewNotificationLogDetail.visibility(false)
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).imageViewMarkAllAsRead().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_dashboard_header_notifications),
            true
        )
        getNotificationLogDetail()
    }

    private fun getNotificationLogDetail() {
        viewModel.getNotificationLogDetail(arguments?.getLong(EXTRA_ID) ?: 0L)
    }

    private fun initNotificationLogDetail(notificationLogDto: NotificationLogDto) {
        if (arguments?.getBoolean(EXTRA_IS_READ) == false) {
            eventBus.actionSyncEvent.emmit(
                BaseEvent(
                    ActionSyncEvent.ACTION_UPDATE_NOTIFICATION_LOG_ITEM,
                    notificationLogDto.id.toString()
                )
            )
        }
        scrollViewNotificationLogDetail.visibility(true)
        when (notificationLogDto.notificationLogType) {
            NotificationLogTypeEnum.ANNOUNCEMENT -> {
                imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_announcement)
                buttonNotificationLogDetail.visibility(false)
            }
            NotificationLogTypeEnum.NEW_DEVICE_LOGIN -> {
                imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_new_device)
                buttonNotificationLogDetail.visibility(true)
                buttonNotificationLogDetail.setOnClickListener {
                    (activity as DashboardActivity).bottomNavigationBTR().currentItem = 4
                    eventBus.fragmentSettingsSyncEvent.emmit(
                        BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_MANAGE_DEVICES)
                    )
                }
            }
            NotificationLogTypeEnum.FILE_UPLOADED -> {
                imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_file_uploaded)
                buttonNotificationLogDetail.visibility(false)
            }
            NotificationLogTypeEnum.NEW_FEATURES -> {
                imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_new_feature)
                buttonNotificationLogDetail.visibility(false)
            }
            else -> {
                imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_announcement)
                buttonNotificationLogDetail.visibility(false)
            }
        }
        textViewNotificationLogDetailCreatedDate.text = viewUtil.getDateFormatByDateString(
            notificationLogDto.createdDate,
            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
            ViewUtil.DATE_FORMAT_DEFAULT
        )
        textViewNotificationLogDetailTitle.text = notificationLogDto.title
        textViewNotificationLogDetailDesc.text = notificationLogDto.message
    }

    private fun showLoading() {
        scrollViewNotificationLogDetail.visibility(false)
        viewLoadingState.visibility(true)
    }

    private fun dismissLoading() {
        viewLoadingState.visibility(false)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_IS_READ = "is_read"
    }
}
