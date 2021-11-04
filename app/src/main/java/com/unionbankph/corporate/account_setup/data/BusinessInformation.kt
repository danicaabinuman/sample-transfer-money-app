package com.unionbankph.corporate.account_setup.data

import kotlinx.serialization.Serializable

@Serializable
data class BusinessInformation (
    var businessName: String? = null,
    var line1: String? = null,
    var line2: String? = null,
    var region: String? = null,
    var city: String? = null,
    var zipCode: String? = null,
)