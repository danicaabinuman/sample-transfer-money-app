package com.unionbankph.corporate.settings.presentation.security.device

import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.settings.data.model.Device
import com.unionbankph.corporate.settings.data.model.ManageDevicesDto
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_manage_devices.*

class ManageDevicesFragment :
    BaseFragment<ManageDevicesViewModel>(R.layout.fragment_manage_devices),
    EpoxyAdapterCallback<Device> {

    private val controller by lazyFast {
        ManageDevicesController(
            applicationContext,
            viewUtil
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[ManageDevicesViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowManageDevicesLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutManageDevices,
                        recyclerViewManageDevices,
                        textViewState
                    )
                }
                is ShowManageDevicesDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutManageDevices,
                        recyclerViewManageDevices
                    )
                }
                is ShowManageDeviceGetDevices -> {
                    updateController(it.manageDevicesDto)
                }
                is ShowManageDevicesError -> {
                    (activity as DashboardActivity).handleOnError(it.throwable)
                }
            }
        })
        viewModel.getManageDevicesList()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_manage_devices),
            hasBackButton = true,
            hasMenuItem = false
        )
        initRecyclerView()
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        recyclerViewManageDevices.setController(controller)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        swipeRefreshLayoutManageDevices.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                viewModel.getManageDevicesList()
            }
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_MANAGE_DEVICES) {
                swipeRefreshLayoutManageDevices.isRefreshing = true
                viewModel.getManageDevicesList()
            }
        }.addTo(disposables)
    }

    override fun onClickItem(view: View, data: Device, position: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        eventBus.fragmentSettingsSyncEvent.emmit(
            BaseEvent(
                FragmentSettingsSyncEvent.ACTION_CLICK_MANAGE_DEVICE_DETAIL,
                data.id.notNullable()
            )
        )
    }

    private fun updateController(data: ManageDevicesDto) {
        controller.setData(data)
    }
}
