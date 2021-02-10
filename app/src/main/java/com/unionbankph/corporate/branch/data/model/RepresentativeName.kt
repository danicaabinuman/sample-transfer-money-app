package com.unionbankph.corporate.branch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepresentativeName(

    @SerialName("first_name")
    var firstName: String? = null,

    @SerialName("last_name")
    var lastName: String? = null,

    @SerialName("middle_name")
    var middleName: String? = null
)
