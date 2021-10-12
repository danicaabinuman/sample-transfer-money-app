package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R

data class LoanTerms(
    val id: Int,
    val term: String,
    val effectiveRate: String
) {
    companion object {
        fun generateLoanTerms(context: Context): List<LoanTerms> {
            return listOf(
                LoanTerms(
                    id = 0,
                    term = context.getString(R.string.title_term),
                    effectiveRate = context.getString(R.string.title_effective_rate)
                ),
                LoanTerms(
                    id = 1,
                    term = context.getString(R.string.label_three_months),
                    effectiveRate = context.getString(R.string.label_one_point_five)
                ),
                LoanTerms(
                    id = 2,
                    term = context.getString(R.string.label_six_months),
                    effectiveRate = context.getString(R.string.label_one_point_six)
                ),
                LoanTerms(
                    id = 3,
                    term = context.getString(R.string.label_nine_months),
                    effectiveRate = context.getString(R.string.label_one_point_seven)
                ),
                LoanTerms(
                    id = 4,
                    term = context.getString(R.string.label_twelve_months),
                    effectiveRate = context.getString(R.string.label_one_point_eight)
                ),
                LoanTerms(
                    id = 5,
                    term = context.getString(R.string.label_twenty_four_months),
                    effectiveRate = context.getString(R.string.label_one_point_nine)
                ),
                LoanTerms(
                    id = 6,
                    term = context.getString(R.string.label_thirty_months),
                    effectiveRate = context.getString(R.string.label_two_point_zero)
                )
            )
        }
    }

}
