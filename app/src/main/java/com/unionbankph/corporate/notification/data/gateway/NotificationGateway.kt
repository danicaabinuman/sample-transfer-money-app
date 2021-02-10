package com.unionbankph.corporate.notification.data.gateway

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import com.unionbankph.corporate.notification.data.model.NotificationLogBadgeCount
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.data.model.OrganizationNotification
import io.reactivex.Single

interface NotificationGateway {

    fun getOrganizationNotification(orgId: String): Single<OrganizationNotification>

    fun getOrganizationNotification(): Single<MutableList<OrganizationNotification>>

    fun getNotifications(): Single<NotificationDto>

    fun getNotification(id: String): Single<Notification>

    fun updateNotificationSettings(notificationForm: NotificationForm): Single<NotificationDto>

    fun getNotificationLogs(pageable: Pageable): Single<PagedDto<NotificationLogDto>>

    fun getNotificationLogDetail(id: Long): Single<NotificationLogDto>

    fun getNotificationLogBadgeCount(): Single<NotificationLogBadgeCount>

    fun markAllAsReadNotificationLogs(): Single<NotificationLogBadgeCount>
}
