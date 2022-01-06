package com.unionbankph.corporate.feature.account_setup

import androidx.annotation.IntDef

@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    AccountPurposeField.PURPOSE,
    AccountPurposeField.SPECIFICATION,
    AccountPurposeField.AMOUNT
)
annotation class AccountPurposeField {
    companion object {
        const val PURPOSE = 0
        const val SPECIFICATION = 1
        const val AMOUNT = 2
    }
}

data class AccountPurposeForm(
    var purpose: String? = null,
    var specification: String? = null,
    var amount: Double? = null,
    var formField: Int? = null,
)

data class AccountPurposeState(
    var purposeError: String? = null,
    var specificationError: String? = null,
    var isDataValid: Boolean = false
)