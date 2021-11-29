package com.unionbankph.corporate.feature.loan

import androidx.annotation.IntDef


@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    FinancialInformationField.OCCUPATION,
    FinancialInformationField.POSITION,
    FinancialInformationField.MONTHLY_INCOME,
    FinancialInformationField.TIN_ID,
    FinancialInformationField.SSS_ID,
    FinancialInformationField.GSIS_ID,
)
annotation class FinancialInformationField {
    companion object {
        const val OCCUPATION = 0
        const val POSITION = 1
        const val MONTHLY_INCOME = 2
        const val TIN_ID = 3
        const val SSS_ID = 4
        const val GSIS_ID = 5
    }
}

data class FinancialInformationForm(
    var occupation: String? = null,
    var position: String? = null,
    var monthlyIncome: String? = null,
    var tinId: String? = null,
    var sssId: String? = null,
    var gsisId: String? = null,
    var formField: Int? = null,
)

data class FinancialInformationState(
    var occupationError: String? = null,
    var positionError: String? = null,
    var monthlyIncomeError: String? = null,
    var tinIdError: String? = null,
    var sssIdError: String? = null,
    var gsisIdError: String? = null,
    var isDataValid: Boolean = false
)