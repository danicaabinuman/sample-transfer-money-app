package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushNotificationPayload(

    @SerialName("title")
    var title: String? = null,

    @SerialName("message")
    var message: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("channel")
    var channel: String? = null,

    @SerialName("role_id")
    var roleId: String? = null
)
