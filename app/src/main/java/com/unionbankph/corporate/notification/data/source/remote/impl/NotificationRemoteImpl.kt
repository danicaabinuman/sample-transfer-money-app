package com.unionbankph.corporate.notification.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import com.unionbankph.corporate.notification.data.model.NotificationLogBadgeCount
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.data.model.OrganizationNotification
import com.unionbankph.corporate.notification.data.source.remote.NotificationRemote
import com.unionbankph.corporate.notification.data.source.remote.client.NotificationApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-17
 */
class NotificationRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : NotificationRemote {

    private val notificationApiClient: NotificationApiClient =
        retrofit.create(NotificationApiClient::class.java)

    override fun getOrganizationNotification(
        accessToken: String,
        orgId: String
    ): Single<Response<MutableList<OrganizationNotification>>> {
        return notificationApiClient.getOrganizationNotification(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getOrganizationNotification(accessToken: String): Single<Response<MutableList<OrganizationNotification>>> {
        return notificationApiClient.getOrganizationNotification(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getNotifications(accessToken: String): Single<Response<NotificationDto>> {
        return notificationApiClient.getNotifications(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getNotification(accessToken: String, id: String): Single<Response<Notification>> {
        return notificationApiClient.getNotification(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun updateNotificationSettings(
        accessToken: String,
        notificationForm: NotificationForm
    ): Single<Response<NotificationDto>> {
        return notificationApiClient.updateNotificationSettings(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            notificationForm
        )
    }

    override fun getNotificationLogs(
        accessToken: String,
        pageable: Pageable
    ): Single<Response<PagedDto<NotificationLogDto>>> {
        return notificationApiClient.getNotificationLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            pageable.page,
            pageable.size
        )
    }

    override fun getNotificationLogDetail(
        accessToken: String,
        id: Long
    ): Single<Response<NotificationLogDto>> {
        return notificationApiClient.getNotificationLogDetail(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getNotificationLogBadgeCount(accessToken: String): Single<Response<NotificationLogBadgeCount>> {
        return notificationApiClient.getNotificationLogBadgeCount(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun markAllAsReadNotificationLogs(accessToken: String): Single<Response<NotificationLogBadgeCount>> {
        return notificationApiClient.markAllAsReadNotificationLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }
}
