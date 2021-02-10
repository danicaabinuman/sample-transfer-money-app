package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationMergeAccountForm(
    @SerialName("corp_id")
    var corpId: String? = null,

    @SerialName("user_id")
    var userId: String? = null,

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("password")
    var password: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null,

    @SerialName("temporary_corporate_user_id")
    var temporaryCorporateUserId: String? = null
)
