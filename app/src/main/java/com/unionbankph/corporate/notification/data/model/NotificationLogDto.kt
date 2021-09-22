package com.unionbankph.corporate.notification.data.model

import android.os.Parcelable
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NotificationLogDto(

    @SerialName("id")
    var id: Long? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("channel")
    var channel: String? = null,

    @SerialName("corporate_user_id")
    var corporateUserId: String? = null,

    @SerialName("title")
    var title: String? = null,

    @SerialName("message")
    var message: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("transaction_id")
    var transactionId: String? = null,

    @SerialName("notification_log_type")
    var notificationLogType: NotificationLogTypeEnum? = null,

    @SerialName("role_id")
    var roleId: String? = null,

    @SerialName("is_read")
    var isRead: Boolean = false
) : Parcelable
