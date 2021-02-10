package com.unionbankph.corporate.dao.domain.form

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/15/20
 */
@Serializable
data class GetCityForm(
    @SerialName("province_id")
    var provinceId: String? = null,
    @SerialName("city_id")
    var cityId: String? = null
)