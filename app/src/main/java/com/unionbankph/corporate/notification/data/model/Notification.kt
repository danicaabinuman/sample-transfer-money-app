package com.unionbankph.corporate.notification.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(

    @SerialName("notification_id")
    var notificationId: Int? = null,

    @SerialName("receive_sms")
    var receiveSms: Boolean = false,

    @SerialName("receive_push")
    var receivePush: Boolean = false,

    @SerialName("receive_email")
    var receiveEmail: Boolean = false,

    @SerialName("receive_notifications")
    var receiveNotifications: Boolean = false
)
