package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R

data class Eligible(
    val id: Int? = null,
    val title: String? = null,
) {
    companion object {
        fun generateEligible(context: Context): List<Eligible> {
            return listOf(
                Eligible(
                    id = 0,
                    title = context.getString(R.string.label_accomodation)
                ), Eligible(
                    id = 1,
                    title = context.getString(R.string.label_administrative)
                ), Eligible(
                    id = 2,
                    title = context.getString(R.string.label_construction)
                ), Eligible(
                    id = 3,
                    title = context.getString(R.string.label_manufacturing)
                ), Eligible(
                    id = 4,
                    title = context.getString(R.string.label_wholesale_and_retail_trade)
                ), Eligible(
                    id = 5,
                    title = context.getString(R.string.label_others)
                )
            )
        }
    }
}