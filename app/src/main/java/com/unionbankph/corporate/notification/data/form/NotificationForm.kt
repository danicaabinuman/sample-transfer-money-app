package com.unionbankph.corporate.notification.data.form

import com.unionbankph.corporate.notification.data.model.Notification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationForm(

    @SerialName("notifications")
    var notifications: MutableList<Notification>? = null/*mutableListOf()*/,

    @SerialName("receive_all_notifications")
    var receiveAllNotifications: Boolean? = null
)
