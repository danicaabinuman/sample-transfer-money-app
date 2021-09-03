package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Mismatched (
    var firstname: String? = null,
    var lastname: String? = null,
    var birthDate: String? = null
)