package com.unionbankph.corporate.settings.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManageDeviceDetailDto(

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

@Serializable
data class LastAccessed(

    @SerialName("id")
    var id: String? = null,

    @SerialName("login_date")
    var loginDate: String? = null,

    @SerialName("location")
    var location: String? = null,

    @SerialName("login_status")
    var loginStatus: String? = null
)
