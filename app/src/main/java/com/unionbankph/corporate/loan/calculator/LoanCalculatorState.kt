package com.unionbankph.corporate.loan.calculator

import com.unionbankph.corporate.account.data.model.Account
import kotlinx.serialization.Serializable

@Serializable
data class LoanCalculatorState (
    var amount: Int? = null,
    var principal: Int? = null,
    var interest: Int? = null,
    var month: Int? = null,
    var monthlyPayment: Int? = null,
    var totalAmountPayable: Int? = null
)