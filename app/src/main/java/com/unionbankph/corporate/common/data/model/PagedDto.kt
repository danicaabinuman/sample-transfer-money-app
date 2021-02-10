package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagedDto<T>(
    @SerialName("results")
    var results: MutableList<T> = mutableListOf(),
    @SerialName("hasNextPage")
    var hasNextPage: Boolean = false,
    @SerialName("totalElements")
    var totalElements: Int = 0,
    @SerialName("currentPage")
    var currentPage: Int = 0,
    @SerialName("pageSize")
    var pageSize: Int = 0,
    @SerialName("totalPages")
    var totalPages: Int = 0
)
