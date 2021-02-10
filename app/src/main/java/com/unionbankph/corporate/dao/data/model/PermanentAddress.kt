package com.unionbankph.corporate.dao.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 5/13/20
 */
@Serializable
data class PermanentAddress(
    @SerialName("barangay")
    var barangay: String? = null,
    @SerialName("city")
    var city: CityDto? = null,
    @SerialName("province")
    var province: ProvinceDto? = null,
    @SerialName("street")
    var street: String? = null,
    @SerialName("unit_or_house_no")
    var unitOrHouseNo: String? = null,
    @SerialName("zip_code")
    var zipCode: String? = null,
    @SerialName("country")
    var country: CountryDto? = null,
    @SerialName("other")
    var otherAddress: OtherAddress? = null
)