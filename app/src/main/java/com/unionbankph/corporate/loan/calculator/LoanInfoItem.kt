package com.unionbankph.corporate.loan.calculator

data class LoanInfoItem(
    val loanAmount: Float? = null,
    val loanTenure: Int? = null,
    var annualInterestRate: Float? = null,
    var monthlyPayment: Float? = null,
    var totalInterestPayable: Float? = null,
    var totalAmountPayable: Float? = null
)