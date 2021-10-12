package com.unionbankph.corporate.loan.calculator

import android.content.Context
import com.unionbankph.corporate.R

data class LoanInfo(
    val id: Int,
    val title: String,
    val value: String,
    /*  val annualInterestRate: Int,
      val monthlyPayment: Int,
      val totalInterestPayable: Int,
      val totalAmountPayable: Int,*/
) {
    companion object {
        fun generateLoanInfo(context: Context): List<LoanInfo> {
            return listOf(
                LoanInfo(
                    id = 0,
                    title = context.getString(R.string.title_loan_amount),
                    value = context.getString(R.string.label_loan_amount_value)
                ),
                LoanInfo(
                    id = 1,
                    title = context.getString(R.string.title_loan_tenure),
                    value = context.getString(R.string.label_twelve_months)
                ),
                LoanInfo(
                    id = 2,
                    title = context.getString(R.string.title_annual_interest_rate),
                    value = context.getString(R.string.label_loan_amount_value)
                ),
                LoanInfo(
                    id = 3,
                    title = context.getString(R.string.title_monthly_payment),
                    value = context.getString(R.string.label_loan_amount_value)
                ),
                LoanInfo(
                    id = 4,
                    title = context.getString(R.string.title_total_interest_payable),
                    value = context.getString(R.string.label_loan_amount_value)
                ),
                LoanInfo(
                    id = 5,
                    title = context.getString(R.string.title_total_amount_payable),
                    value = context.getString(R.string.label_loan_amount_value)
                ),
            )
        }
    }
}
