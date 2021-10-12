package com.unionbankph.corporate.loan.calculator

import android.content.Context
import com.unionbankph.corporate.R

data class MonthlyPaymentBreakdown(
    val id: Int,
    val interest: Int,
    val principal: Int
) {
    companion object {
        fun generateMonthlyPaymentBreakdown(context: Context): List<MonthlyPaymentBreakdown> {
            return listOf(
                MonthlyPaymentBreakdown(
                    id = 0,
                    interest = 75,
                    principal = 25
                )
            )
        }
    }
}
