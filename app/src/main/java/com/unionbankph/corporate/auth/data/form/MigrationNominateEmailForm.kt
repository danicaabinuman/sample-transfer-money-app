package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationNominateEmailForm(

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null
)
