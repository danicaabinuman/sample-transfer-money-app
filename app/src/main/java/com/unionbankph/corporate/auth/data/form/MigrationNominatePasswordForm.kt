package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationNominatePasswordForm(

    @SerialName("password")
    var password: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null
)
