package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 18/03/2019
 */
@Serializable
data class ServiceFee(

    @SerialName("currency")
    val currency: String? = null,

    @SerialName("value")
    val value: String? = null
)
