package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginMigrationDto(

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("temporary_corporate_user_id")
    var temporaryCorporateUserId: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null,

    @SerialName("first_name")
    var firstName: String? = null,

    @SerialName("last_name")
    var lastName: String? = null,

    @SerialName("corp_id")
    var corpId: String? = null,

    @SerialName("user_id")
    var userId: String? = null
)
