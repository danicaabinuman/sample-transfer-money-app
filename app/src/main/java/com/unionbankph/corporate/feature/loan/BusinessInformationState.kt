package com.unionbankph.corporate.feature.loan

import androidx.annotation.IntDef


@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    BusinessInformationField.LEGAL_NAME,
    BusinessInformationField.INDUSTRY,
    BusinessInformationField.ORGANIZATION,
    BusinessInformationField.NUMBER_OF_EMPLOYEE,
    BusinessInformationField.BUSINESS_TIN,
    BusinessInformationField.DATE_STARTED_BUSINESS,
    BusinessInformationField.MOBILE,
    BusinessInformationField.LANDLINE,
    BusinessInformationField.EMAIL
)
annotation class BusinessInformationField {
    companion object {
        const val LEGAL_NAME = 0
        const val INDUSTRY = 1
        const val ORGANIZATION = 2
        const val NUMBER_OF_EMPLOYEE = 3
        const val BUSINESS_TIN = 4
        const val DATE_STARTED_BUSINESS = 5
        const val MOBILE = 6
        const val LANDLINE = 7
        const val EMAIL = 8
    }
}

data class BusinessInformationForm(
    var legalname: String? = null,
    var industry: String? = null,
    var organization: String? = null,
    var numberOfEmployee: String? = null,
    var businessTin: String? = null,
    var dateStartedBusiness: String? = null,
    var mobile: String? = null,
    var landline: String? = null,
    var email: String? = null,
    var formField: Int? = null,
)

data class BusinessInformationState(
    var legalnameError: String? = null,
    var industryError: String? = null,
    var organizationError: String? = null,
    var numberOfEmployeeError: String? = null,
    var businessTinError: String? = null,
    var dateStartedBusinessError: String? = null,
    var mobileError: String? = null,
    var landlineError: String? = null,
    var emailError: String? = null,
    var isDataValid: Boolean = false
)