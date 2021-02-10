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
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.NotificationDto
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_notification.*

class NotificationFragment :
    BaseFragment<NotificationViewModel>(R.layout.fragment_notification),
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
        viewModel = ViewModelProviders.of(this, viewModelFactory)[NotificationViewModel::class.java]
        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowNotificationLoadingSubmit -> {
                    showProgressAlertDialog(NotificationDetailFragment::class.java.simpleName)
                }
                is ShowNotificationDismissLoadingSubmit -> {
                    dismissProgressAlertDialog()
                }
                is ShowNotificationLoading -> {
                    if (!swipeRefreshLayoutNotification.isRefreshing) {
                        viewLoadingState?.visibility = View.VISIBLE
                    }
                    if (viewLoadingState?.visibility == View.VISIBLE) {
                        swipeRefreshLayoutNotification.isEnabled = true
                    }
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutNotification,
                        recyclerViewNotification,
                        null
                    )
                }
                is ShowNotificationDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutNotification,
                        recyclerViewNotification
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
        recyclerViewNotification.itemAnimator = null
        controller.setAdapterCallbacks(this)
        recyclerViewNotification.setController(controller)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        swipeRefreshLayoutNotification.apply {
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
                recyclerViewNotification[0].findViewById<SwitchMaterial>(R.id.switchCompat)
            switchCompat.tag = NotificationController::class.java.simpleName
            switchCompat.isChecked =
                !currentSwitch?.isChecked!!
            updateController()
        }
    }
}
