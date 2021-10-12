package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R

data class FinanceWithUs(
    val id: Int,
    val title: String,
    val ubmsmeTitle: String? = null,
    val ubmsmeIcon: Int? = 0,
    val othersTitle: String? = null,
    val othersIcon: Int? = 0
) {
    companion object {
        fun generateFinanceWithUs(context: Context): List<FinanceWithUs> {
            return listOf(
                FinanceWithUs(
                    id = 0,
                    title = context.getString(R.string.title_simple_and_online_application),
                    ubmsmeTitle = null,
                    ubmsmeIcon = R.drawable.ic_orange_check,
                    othersTitle = null,
                    othersIcon = R.drawable.ic_black_close
                ),
                FinanceWithUs(
                    id = 1,
                    title = context.getString(R.string.title_no_hidden_fees),
                    ubmsmeTitle = null,
                    ubmsmeIcon = R.drawable.ic_orange_check,
                    othersTitle = null,
                    othersIcon = R.drawable.ic_black_close
                ),
                FinanceWithUs(
                    id = 2,
                    title = context.getString(R.string.title_fast_processing),
                    ubmsmeTitle = null,
                    ubmsmeIcon = R.drawable.ic_orange_check,
                    othersTitle = null,
                    othersIcon = R.drawable.ic_black_close
                ),
                FinanceWithUs(
                    id = 3,
                    title = context.getString(R.string.title_time_taken),
                    ubmsmeTitle = context.getString(R.string.title_mins),
                    ubmsmeIcon = null,
                    othersTitle = context.getString(R.string.title_days),
                    othersIcon = null
                ),
                FinanceWithUs(
                    id = 4,
                    title = context.getString(R.string.title_time_taken),
                    ubmsmeTitle = context.getString(R.string.title_five_days),
                    ubmsmeIcon = null,
                    othersTitle = context.getString(R.string.title_thirty_days),
                    othersIcon = null
                )
            )
        }
    }
}