package com.unionbankph.corporate.notification.data.source.remote

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import com.unionbankph.corporate.notification.data.model.NotificationLogBadgeCount
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.data.model.OrganizationNotification
import io.reactivex.Single
import retrofit2.Response

/**
 * Created by herald25santos on 2020-01-17
 */
interface NotificationRemote {

    fun getOrganizationNotification(
        accessToken: String,
        orgId: String
    ): Single<Response<MutableList<OrganizationNotification>>>

    fun getOrganizationNotification(accessToken: String): Single<Response<MutableList<OrganizationNotification>>>

    fun getNotifications(accessToken: String): Single<Response<NotificationDto>>

    fun getNotification(accessToken: String, id: String): Single<Response<Notification>>

    fun updateNotificationSettings(
        accessToken: String,
        notificationForm: NotificationForm
    ): Single<Response<NotificationDto>>

    fun getNotificationLogs(
        accessToken: String,
        pageable: Pageable
    ): Single<Response<PagedDto<NotificationLogDto>>>

    fun getNotificationLogDetail(
        accessToken: String,
        id: Long
    ): Single<Response<NotificationLogDto>>

    fun getNotificationLogBadgeCount(accessToken: String): Single<Response<NotificationLogBadgeCount>>

    fun markAllAsReadNotificationLogs(accessToken: String): Single<Response<NotificationLogBadgeCount>>
}
