package com.unionbankph.corporate.notification.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationLogBadgeCount(

    @SerialName("notification_log_badge_count")
    var notificationLogBadgeCount: Int = 0
)
