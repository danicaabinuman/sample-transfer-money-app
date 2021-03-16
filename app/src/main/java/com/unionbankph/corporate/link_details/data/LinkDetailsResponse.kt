package com.unionbankph.corporate.link_details.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkDetailsResponse(
    @SerialName("id")
    var id: String? = null,

    @SerialName("referenceId")
    var referenceId: Int? = null,

    @SerialName("amount")
    var amount: Double? = null,

    @SerialName("description")
    var description: String? = null,

    @SerialName("note")
    var note: String? = null,

    @SerialName("expireDate")
    var expireDate: String? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("link")
    var link: String? = null,

    @SerialName("createdBy")
    var createdBy: String? = null,

    @SerialName("createdDate")
    var createdDate: String? = null,

    @SerialName("modifiedBy")
    var modifiedBy: String? = null,

    @SerialName("modifiedDate")
    var modifiedDate: String? = null
)