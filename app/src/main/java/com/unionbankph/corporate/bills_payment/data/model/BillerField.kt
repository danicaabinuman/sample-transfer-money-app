package com.unionbankph.corporate.bills_payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillerField(

    @SerialName("description")
    var description: String? = null,

    @SerialName("index")
    var index: String? = null,

    @SerialName("mask")
    var mask: Boolean? = null,

    @SerialName("max")
    var max: Int? = null,

    @SerialName("min")
    var min: Int? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("options")
    var options: MutableList<Option>? = null,

    @SerialName("parent_field_index")
    var parentFieldIndex: Int? = null,

    @SerialName("required")
    var required: Boolean? = null,

    @SerialName("sample")
    var sample: String? = null,

    @SerialName("type")
    var type: String? = null
)
