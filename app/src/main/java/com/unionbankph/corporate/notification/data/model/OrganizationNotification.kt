package com.unionbankph.corporate.notification.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationNotification(
    @SerialName("persist")
    var persist: Int = 0,
    @SerialName("colored")
    var colored: Boolean = false,
    @SerialName("organization_id")
    var organizationId: String? = null,
    @SerialName("organization_name")
    var organizationName: String? = null
)
