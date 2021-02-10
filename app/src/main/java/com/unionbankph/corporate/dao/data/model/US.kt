package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class US(
    @SerialName("record_type")
    var recordType: String? = null,
    @SerialName("citizenship")
    var citizenship: Boolean? = null
)