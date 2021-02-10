package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationTransferFilter(
    @SerialName("title")
    val title: String? = null,
    @SerialName("status")
    val status: String? = null
)
