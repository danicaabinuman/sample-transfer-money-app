package com.unionbankph.corporate.notification.presentation.notification_log.notification_log_detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.unionbankph.corporate.databinding.FragmentNotificationLogBinding
import com.unionbankph.corporate.databinding.FragmentNotificationLogDetailBinding
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.presentation.notification_log.GetNotificationLogDetail
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogError
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogViewModel
import com.unionbankph.corporate.notification.presentation.notification_log.ShowDismissLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowLoading

class NotificationLogDetailFragment :
    BaseFragment<FragmentNotificationLogDetailBinding, NotificationLogViewModel>() {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
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
                    binding.scrollViewNotificationLogDetail.visibility(false)
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).btnHelp().visibility = View.GONE
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
        binding.scrollViewNotificationLogDetail.visibility(true)
        when (notificationLogDto.notificationLogType) {
            NotificationLogTypeEnum.ANNOUNCEMENT -> {
                binding.imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_announcement)
                binding.buttonNotificationLogDetail.visibility(false)
            }
            NotificationLogTypeEnum.NEW_DEVICE_LOGIN -> {
                binding.imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_new_device)
                binding.buttonNotificationLogDetail.visibility(true)
                binding.buttonNotificationLogDetail.setOnClickListener {
                    (activity as DashboardActivity).bottomNavigationBTR().currentItem = 4
                    eventBus.fragmentSettingsSyncEvent.emmit(
                        BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_MANAGE_DEVICES)
                    )
                }
            }
            NotificationLogTypeEnum.FILE_UPLOADED -> {
                binding.imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_file_uploaded)
                binding.buttonNotificationLogDetail.visibility(false)
            }
            NotificationLogTypeEnum.NEW_FEATURES -> {
                binding.imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_new_feature)
                binding.buttonNotificationLogDetail.visibility(false)
            }
            else -> {
                binding.imageViewNotificationDetailLog.setImageResource(R.drawable.ic_notification_announcement)
                binding.buttonNotificationLogDetail.visibility(false)
            }
        }
        binding.textViewNotificationLogDetailCreatedDate.text = viewUtil.getDateFormatByDateString(
            notificationLogDto.createdDate,
            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
            ViewUtil.DATE_FORMAT_DEFAULT
        )
        binding.textViewNotificationLogDetailTitle.text = notificationLogDto.title
        binding.textViewNotificationLogDetailDesc.text = notificationLogDto.message
    }

    private fun showLoading() {
        binding.scrollViewNotificationLogDetail.visibility(false)
        binding.viewLoadingState.viewLoadingLayout.visibility(true)
    }

    private fun dismissLoading() {
        binding.viewLoadingState.viewLoadingLayout.visibility(false)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_IS_READ = "is_read"
    }

    override val viewModelClassType: Class<NotificationLogViewModel>
        get() = NotificationLogViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNotificationLogDetailBinding
        get() = FragmentNotificationLogDetailBinding::inflate
}
