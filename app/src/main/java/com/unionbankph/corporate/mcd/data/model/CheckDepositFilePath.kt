package com.unionbankph.corporate.mcd.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckDepositFilePath(

    @SerialName("file_raw_path")
    var fileRawPath: String? = null,

    @SerialName("file_path")
    var filePath: String? = null
)
