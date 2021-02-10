package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(

    @SerialName("detail")
    var detail: Detail? = null,

    @SerialName("portal_id")
    var portalId: String? = null,

    @SerialName("type")
    var type: String? = null
)
