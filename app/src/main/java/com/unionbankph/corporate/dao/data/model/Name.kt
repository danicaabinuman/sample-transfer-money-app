package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class Name(
    @SerialName("last")
    var last: String? = null,
    @SerialName("middle")
    var middle: String? = null,
    @SerialName("first")
    var first: String? = null
)