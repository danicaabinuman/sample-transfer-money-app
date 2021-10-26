package com.unionbankph.corporate.loan.calculator

interface LoansCalculatorCallback {

    fun onAmountChange(amount: Float)
    fun onMonthsChange(months: Int)
    fun onTotalAmountPayable(totalAmountPayable: Float)
    fun onMonthlyPayment(monthlyPayment: Float)
    fun onPrincipal(principal: Float)
    fun onInterest(interest: Float)
    fun onApplyNow()
}