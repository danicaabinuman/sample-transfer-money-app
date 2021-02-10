package com.unionbankph.corporate.common.data.model

import com.unionbankph.corporate.auth.data.model.Error
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ApiError(
    @SerialName("code")
    var code: Int? = null,
    @SerialName("data")
    var data: String? = null,
    @SerialName("errors")
    var errors: MutableList<Error> = mutableListOf(),
    @SerialName("error")
    var error: String? = null,
    @SerialName("message")
    var message: String? = null,
    @SerialName("status")
    var status: Int? = null,
    @SerialName("error_description")
    var errorDescription: String? = null,
    @SerialName("timestamp")
    var timestamp: String? = null,
    @SerialName("errorCode")
    var errorCode: String? = null,
    @SerialName("path")
    var path: String? = null
)
