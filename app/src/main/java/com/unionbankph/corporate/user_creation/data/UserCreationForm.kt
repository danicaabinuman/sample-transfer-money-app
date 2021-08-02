package com.unionbankph.corporate.user_creation.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreationForm(

    @SerialName("name")
    var name: Name? = null
)
