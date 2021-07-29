package com.unionbankph.corporate.user_creation.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Name(
    @SerialName("first_name")
    var firstName: String? = null,
    @SerialName("last_name")
    var lastName: String? = null
)