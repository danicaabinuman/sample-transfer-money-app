package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R

data class KeyFeatures(
    val id: Int,
    val title: String,
    val icon: Int
) {

    companion object {
        fun generateKeyFeatures(context: Context): List<KeyFeatures> {
            return listOf(
                KeyFeatures(
                    id = 0,
                    title = context.getString(R.string.title_low_interest_rates),
                    icon = R.drawable.ic_low_interest_rates
                ), KeyFeatures(
                    id = 1,
                    title = context.getString(R.string.title_easy_application),
                    icon = R.drawable.ic_easy_application_loan
                ), KeyFeatures(
                    id = 2,
                    title = context.getString(R.string.title_fast_approval),
                    icon = R.drawable.ic_fast_approval
                ), KeyFeatures(
                    id = 3,
                    title = context.getString(R.string.title_no_collateral),
                    icon = R.drawable.ic_no_collateral
                )
            )
        }
    }
}