package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Option(

    @SerialName("action")
    var action: String? = null,

    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("parent_field_index")
    var parentFieldIndex: Int? = null,

    @SerialName("parent_id")
    var parentId: Int? = null,

    @SerialName("status")
    var status: String? = null,

    @SerialName("value")
    var value: String? = null
)
