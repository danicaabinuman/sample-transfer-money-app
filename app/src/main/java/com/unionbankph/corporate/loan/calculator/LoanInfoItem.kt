package com.unionbankph.corporate.loan.calculator

data class LoanInfoItem(
    val loanAmount: Int? = null,
    val loanTenure: Int? = null,
    var annualInterestRate: Int? = null,
    var monthlyPayment: Int? = null,
    var totalInterestPayable: Int? = null,
    var totalAmountPayable: Int? = null
)