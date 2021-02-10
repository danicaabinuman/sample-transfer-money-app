package com.unionbankph.corporate.settings.presentation.security.device

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed4EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.DEVICE_ANDROID
import com.unionbankph.corporate.app.common.extension.DEVICE_BROWSER
import com.unionbankph.corporate.app.common.extension.DEVICE_IOS
import com.unionbankph.corporate.app.common.extension.DEVICE_STATUS_TRUSTED
import com.unionbankph.corporate.app.common.extension.DEVICE_STATUS_UNTRUSTED
import com.unionbankph.corporate.app.common.extension.DEVICE_TABLET
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.HeaderTitleModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ItemStateModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.ItemState
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.settings.data.model.Device
import com.unionbankph.corporate.settings.data.model.LastAccessed
import kotlinx.android.synthetic.main.header_manage_device_detail.view.*
import kotlinx.android.synthetic.main.item_manage_device_detail.view.*
import kotlinx.android.synthetic.main.widget_device_card_view.view.*

class ManageDeviceDetailController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed4EpoxyController<Device, MutableList<LastAccessed>, ItemState, Pageable>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    @AutoModel
    lateinit var headerTitleModel: HeaderTitleModel_

    @AutoModel
    lateinit var itemStateModel: ItemStateModel_

    @AutoModel
    lateinit var lastAccessHeaderTitleModel: HeaderTitleModel_

    @AutoModel
    lateinit var manageDevicesDetailHeaderModel: ManageDevicesDetailHeaderModel_

    private lateinit var callbacks: AdapterCallback

    private lateinit var callbacks2: EpoxyAdapterCallback<LastAccessed>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        device: Device,
        deviceLogins: MutableList<LastAccessed>,
        itemState: ItemState,
        pageable: Pageable
    ) {
        val title = when (device.deviceType) {
            DEVICE_STATUS_UNTRUSTED ->
                context.getString(R.string.title_untrusted_devices)
            DEVICE_STATUS_TRUSTED ->
                context.getString(R.string.title_trusted_devices)
            else -> context.getString(R.string.title_browser_access)
        }
        headerTitleModel.title(title).addTo(this)
        manageDevicesDetailHeaderModel
            .device(device)
            .context(context)
            .callbacks(callbacks)
            .addTo(this)
        lastAccessHeaderTitleModel
            .title(context.formatString(R.string.title_login_history))
            .addTo(this)
        deviceLogins.forEachIndexed { index, lastAccessed ->
            manageDevicesDetailItem {
                id(lastAccessed.id)
                lastAccessed(lastAccessed)
                position(index)
            }
        }
        itemStateModel
            .message(itemState.message)
            .addIf(itemState.hasState, this)
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks2)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    interface AdapterCallback {
        fun onClickPositiveButton(device: Device)
        fun onClickNegativeButton(device: Device)
    }

    fun setAdapterCallbacks(callbacks: AdapterCallback) {
        this.callbacks = callbacks
    }

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<LastAccessed>) {
        this.callbacks2 = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.header_manage_device_detail)
abstract class ManageDevicesDetailHeaderModel :
    EpoxyModelWithHolder<ManageDevicesDetailHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var device: Device

    @EpoxyAttribute
    lateinit var callbacks: ManageDeviceDetailController.AdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            textViewDevice.text = device.userAgent
            textViewDeviceUpdate.text = device.loginDate
            imageViewDevice.setImageResource(
                when (device.devicePlatform) {
                    DEVICE_ANDROID -> {
                        R.drawable.ic_device_android
                    }
                    DEVICE_IOS -> {
                        R.drawable.ic_device_iphone
                    }
                    DEVICE_TABLET -> {
                        R.drawable.ic_device_tablet
                    }
                    DEVICE_BROWSER -> {
                        R.drawable.ic_device_browser
                    }
                    else -> {
                        R.drawable.ic_device_android
                    }
                }
            )
            if (device.devicePlatform == DEVICE_BROWSER) {
                buttonTrustedDevice.visibility = View.GONE
                buttonForgetDevice.visibility = View.GONE
            } else {
                buttonTrustedDevice.visibility = View.VISIBLE
                buttonForgetDevice.visibility = View.VISIBLE
            }
            buttonTrustedDevice.text = if (device.deviceType == DEVICE_STATUS_TRUSTED) {
                context.formatString(R.string.action_untrust_device)
            } else {
                context.formatString(R.string.action_trust_device)
            }
            buttonTrustedDevice.setOnClickListener {
                callbacks.onClickPositiveButton(device)
            }
            buttonForgetDevice.setOnClickListener {
                callbacks.onClickNegativeButton(device)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var imageViewDevice: ImageView
        lateinit var textViewDevice: TextView
        lateinit var textViewDeviceUpdate: TextView
        lateinit var buttonTrustedDevice: Button
        lateinit var buttonForgetDevice: Button

        override fun bindView(itemView: View) {
            imageViewDevice = itemView.imageViewDevice
            textViewDevice = itemView.textViewDevice
            textViewDeviceUpdate = itemView.textViewDeviceUpdate
            buttonTrustedDevice = itemView.buttonTrustedDevice
            buttonForgetDevice = itemView.buttonForgetDevice
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_manage_device_detail)
abstract class ManageDevicesDetailItemModel :
    EpoxyModelWithHolder<ManageDevicesDetailItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var lastAccessed: LastAccessed

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.apply {
            viewBorderTop.visibility(position == 0)
            textViewDate.text = lastAccessed.loginDate
            textViewLocation.text = lastAccessed.location
            textViewStatus.text = lastAccessed.loginStatus?.toLowerCase()?.capitalize()
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var viewBorderTop: View
        lateinit var viewBorderBottom: View
        lateinit var textViewDate: TextView
        lateinit var textViewLocation: TextView
        lateinit var textViewStatus: TextView

        override fun bindView(itemView: View) {
            viewBorderTop = itemView.viewBorderTop
            viewBorderBottom = itemView.viewBorderBottom
            textViewDate = itemView.textViewDate
            textViewLocation = itemView.textViewLocation
            textViewStatus = itemView.textViewStatus
        }
    }
}
