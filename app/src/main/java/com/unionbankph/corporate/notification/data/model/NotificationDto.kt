package com.unionbankph.corporate.notification.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(

    @SerialName("notifications")
    var notifications: MutableList<Notification> = mutableListOf(),

    @SerialName("receive_all_notifications")
    var receiveAllNotifications: Boolean? = null
)
