package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class OtherAddress(
    @SerialName("city")
    var city: String? = null,
    @SerialName("province")
    var province: String? = null
)