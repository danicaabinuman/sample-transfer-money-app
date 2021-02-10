package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class Government(
    @SerialName("id")
    var id: Int? = null,
    @SerialName("number")
    var number: String? = null
)