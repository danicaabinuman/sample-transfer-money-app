package com.unionbankph.corporate.notification.data.source.remote.client

import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import com.unionbankph.corporate.notification.data.model.NotificationLogBadgeCount
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.data.model.OrganizationNotification
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiClient {

    @GET("api/{api_version}/notifications")
    fun getOrganizationNotification(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<OrganizationNotification>>>

    @GET("api/{api_version}/config/notifications")
    fun getNotifications(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<NotificationDto>>

    @GET("api/{api_version}/config/notifications/{id}")
    fun getNotification(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Notification>>

    @POST("api/{api_version}/config/notifications")
    fun updateNotificationSettings(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        notificationForm: NotificationForm
    ): Single<Response<NotificationDto>>

    @GET("api/{api_version}/notifications/logs")
    fun getNotificationLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int
    ): Single<Response<PagedDto<NotificationLogDto>>>

    @GET("api/{api_version}/notifications/logs/{id}")
    fun getNotificationLogDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        notificationLogId: Long
    ): Single<Response<NotificationLogDto>>

    @GET("api/{api_version}/notifications/logs/count")
    fun getNotificationLogBadgeCount(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<NotificationLogBadgeCount>>

    @POST("api/{api_version}/notifications/logs/read")
    fun markAllAsReadNotificationLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<NotificationLogBadgeCount>>
}
