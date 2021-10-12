package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R
import kotlinx.serialization.Serializable

@Serializable
data class CommonQuestions(
    val id: Int,
    val title: String,
    var expand: Boolean = false
) {
    companion object {
        fun generateCommonQuestions(context: Context): List<CommonQuestions> {
            return mutableListOf(
                CommonQuestions(
                    id = 0,
                    title = context.getString(R.string.label_how_can_i_get_a_loan),
                ), CommonQuestions(
                    id = 1,
                    title = context.getString(R.string.label_what_are_the_requirements),
                ), CommonQuestions(
                    id = 2,
                    title = context.getString(R.string.label_how_much_can_i_borrow)
                )
            )
        }
    }
}