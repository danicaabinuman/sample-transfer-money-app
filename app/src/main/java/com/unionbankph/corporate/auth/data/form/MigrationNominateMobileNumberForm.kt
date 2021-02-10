package com.unionbankph.corporate.auth.data.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MigrationNominateMobileNumberForm(

    @SerialName("country_code_id")
    var countryCodeId: Int? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("migration_token")
    var migrationToken: String? = null
)
