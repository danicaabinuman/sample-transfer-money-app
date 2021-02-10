package com.unionbankph.corporate.notification.data.gateway.impl

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import com.unionbankph.corporate.notification.data.model.NotificationLogBadgeCount
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.data.model.OrganizationNotification
import com.unionbankph.corporate.notification.data.source.remote.NotificationRemote
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import javax.inject.Inject

class NotificationGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val notificationRemote: NotificationRemote,
    private val settingsGateway: SettingsGateway
) : NotificationGateway {

    override fun getOrganizationNotification(orgId: String): Single<OrganizationNotification> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getOrganizationNotification(it, orgId) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .toObservable()
            .flatMapIterable { it }
            .singleOrError()
    }

    override fun getOrganizationNotification(): Single<MutableList<OrganizationNotification>> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getOrganizationNotification(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getNotifications(): Single<NotificationDto> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getNotifications(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getNotification(id: String): Single<Notification> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getNotification(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun updateNotificationSettings(notificationForm: NotificationForm): Single<NotificationDto> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.updateNotificationSettings(it, notificationForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getNotificationLogs(pageable: Pageable): Single<PagedDto<NotificationLogDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getNotificationLogs(it, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getNotificationLogDetail(id: Long): Single<NotificationLogDto> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getNotificationLogDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getNotificationLogBadgeCount(): Single<NotificationLogBadgeCount> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.getNotificationLogBadgeCount(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun markAllAsReadNotificationLogs(): Single<NotificationLogBadgeCount> {
        return settingsGateway.getAccessToken()
            .flatMap { notificationRemote.markAllAsReadNotificationLogs(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}
