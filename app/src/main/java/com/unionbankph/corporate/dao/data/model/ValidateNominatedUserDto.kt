package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/07/20
 */
@Serializable
data class ValidateNominatedUserDto(
    @SerialName("results")
    var results: Map<String, Results> = mapOf()
)