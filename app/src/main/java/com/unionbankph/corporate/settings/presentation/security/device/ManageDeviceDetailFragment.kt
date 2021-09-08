package com.unionbankph.corporate.settings.presentation.security.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.DEVICE_STATUS_TRUSTED
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.ItemState
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentManageDeviceDetailBinding
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.model.Device
import com.unionbankph.corporate.settings.data.model.LastAccessed

class ManageDeviceDetailFragment :
    BaseFragment<FragmentManageDeviceDetailBinding, ManageDevicesViewModel>(),
    OnConfirmationPageCallBack,
    ManageDeviceDetailController.AdapterCallback, EpoxyAdapterCallback<LastAccessed> {

    private lateinit var device: Device

    private var loginHistories: MutableList<LastAccessed> = mutableListOf()

    private val pageable by lazyFast { Pageable() }

    private val controller by lazyFast {
        ManageDeviceDetailController(
            applicationContext,
            viewUtil
        )
    }

    private val itemState by lazyFast {
        ItemState(
            formatString(R.string.msg_no_login_history),
            loginHistories.isEmpty()
        )
    }

    private val deviceId by lazyFast { arguments?.getString(EXTRA_ID).notNullable() }

    private var confirmationBottomSheet: ConfirmationBottomSheet? = null

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowManageDevicesProgressLoading -> {
                    showProgressAlertDialog(
                        ManageDeviceDetailFragment::class.java.simpleName
                    )
                }
                is ShowManageDevicesProgressDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowManageDevicesLoading -> {
                    showLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        null,
                        binding.recyclerViewManageDeviceDetail,
                        null
                    )
                }
                is ShowManageDevicesDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        null,
                        binding.recyclerViewManageDeviceDetail
                    )
                }
                is ShowManageDevicesEndlessLoading -> {
                    pageable.isLoadingPagination = true
                    updateController()
                }
                is ShowManageDevicesEndlessDismissLoading -> {
                    pageable.isLoadingPagination = false
                    updateController()
                }
                is ShowManageDeviceTrustDevice -> {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(ActionSyncEvent.ACTION_UPDATE_OTP_SETTINGS, "1")
                    )
                    updateDevices()
                }
                is ShowManageDeviceUnTrustDevice -> {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(ActionSyncEvent.ACTION_UPDATE_OTP_SETTINGS, "0")
                    )
                    updateDevices()
                }
                is ShowManageDeviceForget -> {
                    if (it.sameUdid) {
                        viewModel.clearTOTPToken()
                    } else {
                        updateDevices()
                    }
                }
                is ShowManageClearTOTPToken -> {
                    viewModel.logout()
                }
                is ShowManageLogoutUser -> {
                    navigator.navigateClearStacks(
                        getAppCompatActivity(),
                        LoginActivity::class.java,
                        Bundle().apply {
                            putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                        },
                        true,
                        Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
                    )
                }
                is ShowManageDeviceGetDeviceDetail -> {
                    device = it.device
                    getLoginHistory(true)
                }
                is ShowManageDevicesEndlessError -> {
                    with(itemState) {
                        message = it.throwable.message.notNullable()
                        hasState = true
                    }
                    updateController()
                }
                is ShowManageDevicesError -> {
                    (activity as DashboardActivity).handleOnError(it.throwable)
                }
            }
        })
        viewModel.lastAccessedLiveData.observe(this, Observer {
            it?.let { pagedDto ->
                pageable.apply {
                    totalPageCount = pagedDto.totalPages
                    isLastPage = !pagedDto.hasNextPage
                    increasePage()
                }
                if (pagedDto.currentPage == 0) {
                    loginHistories = pagedDto.results
                } else {
                    loginHistories.addAll(pagedDto.results)
                }
                updateController()
            }
        })
        getDeviceDetail()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).btnHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_manage_devices),
            hasBackButton = true,
            hasMenuItem = false
        )
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewManageDeviceDetail.layoutManager = linearLayoutManager
        binding.recyclerViewManageDeviceDetail.addOnScrollListener(
            object : PaginationScrollListener(linearLayoutManager) {
                override val totalPageCount: Int
                    get() = pageable.totalPageCount
                override val isLastPage: Boolean
                    get() = pageable.isLastPage
                override val isLoading: Boolean
                    get() = pageable.isLoadingPagination
                override val isFailed: Boolean
                    get() = pageable.isFailed

                override fun loadMoreItems() {
                    with(itemState) { hasState = false }
                    if (!pageable.isLoadingPagination) getLoginHistory(false)
                }
            }
        )
        binding.recyclerViewManageDeviceDetail.setController(controller)
        controller.setAdapterCallbacks(this)
        controller.setEpoxyAdapterCallback(this)
    }

    override fun onTapToRetry() {
        getLoginHistory(isInitialLoading = false, isTapToRetry = true)
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        confirmationBottomSheet?.dismiss()
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        val device = JsonHelper.fromJson<Device>(data.notNullable())
        val form = ManageDeviceForm(device.id)
        if (tag == TAG_DIALOG_TRUST_DEVICE) {
            if (device.deviceType == DEVICE_STATUS_TRUSTED) {
                viewModel.unTrustDevice(form)
            } else {
                viewModel.trustDevice(form)
            }
        } else if (tag == TAG_DIALOG_FORGET_DEVICE) {
            viewModel.forgetDevice(device)
        }
    }

    override fun onClickPositiveButton(device: Device) {
        val isTrustedDevice = device.deviceType == DEVICE_STATUS_TRUSTED
        val title = if (isTrustedDevice) {
            formatString(R.string.param_title_untrust_device_name, device.userAgent)
        } else {
            formatString(R.string.param_title_trust_device_name, device.userAgent)
        }
        val positiveButtonName = if (isTrustedDevice) {
            getString(R.string.action_untrust)
        } else {
            getString(R.string.action_trust)
        }
        showConfirmationDialog(
            title,
            getString(R.string.desc_dialog_trust_device),
            positiveButtonName,
            device,
            TAG_DIALOG_TRUST_DEVICE
        )
    }

    override fun onClickNegativeButton(device: Device) {
        showConfirmationDialog(
            formatString(R.string.param_title_forget_device_name, device.userAgent),
            getString(R.string.desc_dialog_forget_device),
            getString(R.string.action_forget),
            device,
            TAG_DIALOG_FORGET_DEVICE
        )
    }

    private fun updateController() {
        controller.setData(device, loginHistories, itemState, pageable)
    }

    private fun getDeviceDetail() {
        viewModel.getManageDeviceDetail(deviceId)
    }

    private fun showConfirmationDialog(
        title: String,
        desc: String,
        positiveButtonName: String,
        device: Device,
        tag: String
    ) {
        confirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_device_android_white,
            title,
            desc,
            positiveButtonName,
            getString(R.string.action_not_now),
            JsonHelper.toJson(device)
        )
        confirmationBottomSheet?.setOnConfirmationPageCallBack(this)
        confirmationBottomSheet?.show(childFragmentManager, tag)
    }

    private fun updateDevices() {
        confirmationBottomSheet?.dismiss()
        eventBus.actionSyncEvent.emmit(
            BaseEvent(ActionSyncEvent.ACTION_UPDATE_MANAGE_DEVICES)
        )
        (activity as DashboardActivity).popStackFragmentSettings()
    }

    private fun getLoginHistory(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getLoginHistory(deviceId, pageable, isInitialLoading)
    }

    companion object {

        const val EXTRA_ID = "id"

        const val TAG_DIALOG_FORGET_DEVICE = "dialog_forget_device"
        const val TAG_DIALOG_TRUST_DEVICE = "dialog_trust_device"

        fun newInstance(id: String): ManageDeviceDetailFragment {
            val fragment =
                ManageDeviceDetailFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<ManageDevicesViewModel>
        get() = ManageDevicesViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentManageDeviceDetailBinding
        get() = FragmentManageDeviceDetailBinding::inflate
}
