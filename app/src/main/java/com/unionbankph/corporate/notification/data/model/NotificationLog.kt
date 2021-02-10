package com.unionbankph.corporate.notification.data.model

data class NotificationLog(
    var notificationLogDto: NotificationLogDto,
    var isRead: Boolean = false
)
