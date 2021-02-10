package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManageDevicesDto(

    @SerialName("browser_access")
    var browserAccess: MutableList<Device> = mutableListOf(),

    @SerialName("trusted_devices")
    var trustedDevices: MutableList<Device> = mutableListOf(),

    @SerialName("untrusted_devices")
    var untrustedDevices: MutableList<Device> = mutableListOf()
)

@Serializable
data class Device(

    @SerialName("user_agent")
    var userAgent: String? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("login_date")
    var loginDate: String? = null,

    @SerialName("location")
    var location: String? = null,

    @SerialName("udid")
    var udid: String? = null,

    @SerialName("device_type")
    var deviceType: String? = null,

    @SerialName("device_logins")
    var deviceLogins: MutableList<LastAccessed> = mutableListOf(),

    @SerialName("device_platform")
    var devicePlatform: String? = null
)
