package com.unionbankph.corporate.fund_transfer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Purpose(

    @SerialName("code")
    var code: String? = null,

    @SerialName("description")
    var description: String? = null
)
