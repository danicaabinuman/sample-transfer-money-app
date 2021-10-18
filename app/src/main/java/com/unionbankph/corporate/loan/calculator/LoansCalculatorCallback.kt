package com.unionbankph.corporate.loan.calculator

interface LoansCalculatorCallback {

    fun onAmountChange(amount: Int)
    fun onMonthsChange(months: Int)
    fun onTotalAmountPayable(totalAmountPayable: Int)
    fun onMonthlyPayment(monthlyPayment: Int)
    fun onPrincipal(principal: Int)
    fun onInterest(interest: Int)
    fun onApplyNow()
}