package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationForm(

    @SerialName("code")
    var code: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null,

    @SerialName("request_id")
    var requestId: String? = null
)
