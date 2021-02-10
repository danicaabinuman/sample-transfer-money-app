package com.unionbankph.corporate.dao.domain.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 07/07/20
 */
@Serializable
data class ValidateNominatedUserForm(
    @SerialName("users")
    var users: MutableList<Users> = mutableListOf()
)