package com.unionbankph.corporate.feature.loan

import androidx.annotation.IntDef


@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    BusinessAddressField.ADDRESS_LINE_ONE,
    BusinessAddressField.ADDRESS_LINE_TWO,
    BusinessAddressField.CITY,
    BusinessAddressField.PROVINCE,
    BusinessAddressField.REGION,
    BusinessAddressField.ZIP_CODE,
    BusinessAddressField.ESTABLISHMENT,
    BusinessAddressField.YEARS_IN_OPERATION
)
annotation class BusinessAddressField {
    companion object {
        const val ADDRESS_LINE_ONE = 0
        const val ADDRESS_LINE_TWO = 1
        const val CITY = 2
        const val PROVINCE = 3
        const val REGION = 4
        const val ZIP_CODE = 5
        const val ESTABLISHMENT = 6
        const val YEARS_IN_OPERATION = 7
    }
}

data class BusinessAddressForm(
    var addressLineOne: String? = null,
    var addressLineTwo: String? = null,
    var city: String? = null,
    var province: String? = null,
    var region: String? = null,
    var zipCode: String? = null,
    var establishment: String? = null,
    var yearsInOperation: String? = null,
    var formField: Int? = null,
)

data class BusinessAddressState(
    var addressLineOneError: String? = null,
    var addressLineTwoError: String? = null,
    var cityError: String? = null,
    var provinceError: String? = null,
    var regionError: String? = null,
    var zipCodeError: String? = null,
    var establishmentError: String? = null,
    var yearsInOperationError: String? = null,
    var isDataValid: Boolean = false
)