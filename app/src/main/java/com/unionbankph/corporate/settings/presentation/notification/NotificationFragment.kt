package com.unionbankph.corporate.settings.presentation.notification

import android.view.View
import android.widget.Switch
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.switchmaterial.SwitchMaterial
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ResultSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentNotificationBinding
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.NotificationDto
import io.reactivex.rxkotlin.addTo

class NotificationFragment :
    BaseFragment<FragmentNotificationBinding, NotificationViewModel>(),
    NotificationController.AdapterCallbacks {

    private lateinit var notificationDto: NotificationDto

    private var currentSwitch: SwitchMaterial? = null

    private val controller by lazyFast {
        NotificationController(
            applicationContext
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowNotificationLoadingSubmit -> {
                    showProgressAlertDialog(NotificationDetailFragment::class.java.simpleName)
                }
                is ShowNotificationDismissLoadingSubmit -> {
                    dismissProgressAlertDialog()
                }
                is ShowNotificationLoading -> {
                    if (!binding.swipeRefreshLayoutNotification.isRefreshing) {
                        binding.viewLoadingState.viewLoadingLayout.visibility = View.VISIBLE
                    }
                    if (binding.viewLoadingState.viewLoadingLayout.visibility == View.VISIBLE) {
                        binding.swipeRefreshLayoutNotification.isEnabled = true
                    }
                    showLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        binding.swipeRefreshLayoutNotification,
                        binding.recyclerViewNotification,
                        null
                    )
                }
                is ShowNotificationDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        binding.swipeRefreshLayoutNotification,
                        binding.recyclerViewNotification
                    )
                }
                is ShowNotificationData -> {
                    notificationDto = it.data
                    updateController()
                }
                is ShowNotificationError -> {
                    revertSwitch()
                    (activity as DashboardActivity).handleOnError(it.throwable)
                }
            }
        })
        viewModel.getNotifications()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_notifications),
            hasBackButton = true,
            hasMenuItem = false
        )
        binding.recyclerViewNotification.itemAnimator = null
        controller.setAdapterCallbacks(this)
        binding.recyclerViewNotification.setController(controller)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.swipeRefreshLayoutNotification.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                viewModel.getNotifications()
            }
        }
        eventBus.resultSyncEvent.flowable.subscribe {
            if (it.eventType == ResultSyncEvent.ACTION_UPDATE_NOTIFICATION) {
                val updatedNotificationDto = JsonHelper.fromJson<NotificationDto>(it.payload)
                notificationDto = updatedNotificationDto
                updateController()
            }
        }.addTo(disposables)
    }

    override fun onClickItem(id: Int?) {
        eventBus.fragmentSettingsSyncEvent.emmit(
            BaseEvent(
                FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION_DETAIL,
                id.toString()
            )
        )
    }

    override fun onSlideSwitchCompat(switch: SwitchMaterial, isChecked: Boolean) {
        this.currentSwitch = switch
        viewModel.updateNotificationSettings(
            NotificationForm(notificationDto.notifications, isChecked)
        )
    }

    private fun updateController() {
        controller.setData(notificationDto)
    }

    private fun revertSwitch() {
        if (currentSwitch != null) {
            notificationDto.receiveAllNotifications =
                !currentSwitch?.isChecked!!
            val switchCompat =
                binding.recyclerViewNotification[0].findViewById<SwitchMaterial>(R.id.switchCompat)
            switchCompat.tag = NotificationController::class.java.simpleName
            switchCompat.isChecked =
                !currentSwitch?.isChecked!!
            updateController()
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_notification

    override val viewModelClassType: Class<NotificationViewModel>
        get() = NotificationViewModel::class.java
}
