package com.unionbankph.corporate.feature.loan

import androidx.annotation.IntDef


@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    AddressInformationField.PRESENT_ADDRESS_LINE_ONE,
    AddressInformationField.PRESENT_ADDRESS_LINE_TWO,
    AddressInformationField.PRESENT_ADDRESS_PROVINCE,
    AddressInformationField.PRESENT_ADDRESS_CITY,
    AddressInformationField.PRESENT_ADDRESS_ZIP_CODE,
    AddressInformationField.PERMANENT_ADDRESS_LINE_ONE,
    AddressInformationField.PERMANENT_ADDRESS_LINE_TWO,
    AddressInformationField.PERMANENT_ADDRESS_PROVINCE,
    AddressInformationField.PERMANENT_ADDRESS_CITY,
    AddressInformationField.PERMANENT_ADDRESS_ZIP_CODE
)
annotation class AddressInformationField {
    companion object {
        const val PRESENT_ADDRESS_LINE_ONE = 0
        const val PRESENT_ADDRESS_LINE_TWO = 1
        const val PRESENT_ADDRESS_PROVINCE = 2
        const val PRESENT_ADDRESS_CITY = 3
        const val PRESENT_ADDRESS_ZIP_CODE = 4
        const val PERMANENT_ADDRESS_LINE_ONE = 5
        const val PERMANENT_ADDRESS_LINE_TWO = 6
        const val PERMANENT_ADDRESS_PROVINCE = 7
        const val PERMANENT_ADDRESS_CITY = 8
        const val PERMANENT_ADDRESS_ZIP_CODE = 9
    }
}

data class AddressInformationForm(
    var presentAddressLineOne: String? = null,
    var presentAddressLineTwo: String? = null,
    var presentAddressProvince: String? = null,
    var presentAddressCity: String? = null,
    var presentAddressZipCode: String? = null,
    var permanentAddressLineOne: String? = null,
    var permanentAddressLineTwo: String? = null,
    var permanentAddressProvince: String? = null,
    var permanentAddressCity: String? = null,
    var permanentAddressZipCode: String? = null,
    var formField: Int? = null,
)

data class AddressInformationState(
    var presentAddressLineOneError: String? = null,
    var presentAddressLineTwoError: String? = null,
    var presentAddressProvinceError: String? = null,
    var presentAddressCityError: String? = null,
    var presentAddressZipCodeError: String? = null,
    var permanentAddressLineOneError: String? = null,
    var permanentAddressLineTwoError: String? = null,
    var permanentAddressProvinceError: String? = null,
    var permanentAddressCityError: String? = null,
    var permanentAddressZipCodeError: String? = null,
    var isDataValid: Boolean = false
)