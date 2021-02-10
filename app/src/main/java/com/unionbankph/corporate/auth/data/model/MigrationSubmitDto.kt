package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationSubmitDto(

    @SerialName("temporary_corporate_user_id")
    val temporaryCorporateUserId: String? = null,

    @SerialName("message")
    val message: String? = null
)
