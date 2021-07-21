package com.unionbankph.corporate.settings.presentation.security.device

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.DEVICE_ANDROID
import com.unionbankph.corporate.app.common.extension.DEVICE_BROWSER
import com.unionbankph.corporate.app.common.extension.DEVICE_IOS
import com.unionbankph.corporate.app.common.extension.DEVICE_TABLET
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemManageDeviceBinding
import com.unionbankph.corporate.settings.data.model.Device
import com.unionbankph.corporate.settings.data.model.ManageDevicesDto

class ManageDevicesController
constructor(
        private val context: Context,
        private val viewUtil: ViewUtil
) : TypedEpoxyController<ManageDevicesDto>() {

//    @AutoModel
//    lateinit var headerTrustedTitleModel: HeaderTitleModel_
//
//    @AutoModel
//    lateinit var headerUnTrustedTitleModel: HeaderTitleModel_
//
//    @AutoModel
//    lateinit var headerBrowserTitleModel: HeaderTitleModel_

    private lateinit var callbacks: EpoxyAdapterCallback<Device>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(manageDevicesDto: ManageDevicesDto) {
        headerTrustedTitleModel.title(context.getString(R.string.title_trusted_devices))
                .addIf(manageDevicesDto.trustedDevices.isNotEmpty(), this)
        manageDevicesDto.trustedDevices.forEachIndexed { index, device ->
            manageDevicesItem {
                id("trusted_devices_${device.id}")
                device(device)
                position(index)
                callbacks(callbacks)
            }
        }
        headerUnTrustedTitleModel.title(context.getString(R.string.title_untrusted_devices))
                .addIf(manageDevicesDto.untrustedDevices.isNotEmpty(), this)
        manageDevicesDto.untrustedDevices.forEachIndexed { index, device ->
            manageDevicesItem {
                id("untrusted_devices_${device.id}")
                device(device)
                position(index)
                callbacks(callbacks)
            }
        }
        headerBrowserTitleModel.title(context.getString(R.string.title_browser_access))
                .addIf(manageDevicesDto.browserAccess.isNotEmpty(), this)
        manageDevicesDto.browserAccess.forEachIndexed { index, device ->
            manageDevicesItem {
                id("browser_access_${device.id}")
                device(device)
                position(index)
                callbacks(callbacks)
            }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<Device>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_manage_device)
abstract class ManageDevicesItemModel : EpoxyModelWithHolder<ManageDevicesItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var device: Device

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Device>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
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
            constraintLayoutItem.setOnClickListener {
                callbacks.onClickItem(constraintLayoutItem, device, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemManageDeviceBinding

        override fun bindView(itemView: View) {
            binding = ItemManageDeviceBinding.bind(itemView)
        }
    }
}
