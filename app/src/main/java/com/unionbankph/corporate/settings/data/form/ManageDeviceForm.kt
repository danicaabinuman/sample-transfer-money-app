package com.unionbankph.corporate.settings.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManageDeviceForm(

    @SerialName("device_id")
    var id: String? = null
)
