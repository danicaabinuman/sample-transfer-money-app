package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R

data class Eligible(
    val id: Int,
    val title: String
) {
    companion object {
        fun generateEligible(context: Context): List<Eligible> {
            return listOf(
                Eligible(
                    id = 0,
                    title = context.getString(R.string.label_cafe_restaurant_bakeshop)
                ), Eligible(
                    id = 1,
                    title = context.getString(R.string.label_social_media)
                ), Eligible(
                    id = 2,
                    title = context.getString(R.string.label_moving_and_hauling)
                ), Eligible(
                    id = 3,
                    title = context.getString(R.string.label_ecommerce)
                ), Eligible(
                    id = 4,
                    title = context.getString(R.string.label_travel_planner)
                ), Eligible(
                    id = 5,
                    title = context.getString(R.string.label_dry_cleaning)
                )
            )
        }
    }
}