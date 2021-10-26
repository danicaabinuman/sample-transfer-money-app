package com.unionbankph.corporate.loan.calculator

import com.unionbankph.corporate.account.data.model.Account
import kotlinx.serialization.Serializable

@Serializable
data class LoanCalculatorState (
    var amount: Float? = null,
    var principal: Float? = null,
    var interest: Float? = null,
    var month: Int? = null,
    var monthlyPayment: Float? = null,
    var totalAmountPayable: Float? = null
)