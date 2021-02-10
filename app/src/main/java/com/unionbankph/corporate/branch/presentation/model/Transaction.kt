package com.unionbankph.corporate.branch.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(

    @SerialName("type")
    var type: String? = null,

    @SerialName("detail")
    var detail: Detail? = null
)
