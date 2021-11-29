package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.unionbankph.corporate.R
import kotlinx.serialization.Serializable

//TODO
@Serializable
data class CommonQuestions(
    var id: Int? = null,
    var title: String? = null,
    var header: List<CommonQuestionsHeader>? = null,
    val bullet: List<CommonQuestionsBullet>? = null,
    val subBullet: List<CommonQuestionsSubBullet>? = null,
    var expand: Boolean = false
) {
    companion object {
        fun generateCommonQuestions(context: Context): List<CommonQuestions> {
            return mutableListOf(
                CommonQuestions(
                    id = 0,
                    title = context.getString(R.string.title_am_i_eligible_to_apply),
                    header = listOf(
                        CommonQuestionsHeader(
                            header = null,
                            listOf(CommonQuestionsBullet.generateCommonQuestionsBullet(context)[0])
                        )
                    ),
                    expand = false
                ), CommonQuestions(
                    id = 1,
                    title = context.getString(R.string.title_what_are_the_requirements),
                    header = listOf(
                        CommonQuestionsHeader(
                            header = context.getString(R.string.title_if_individual),
                            listOf(CommonQuestionsBullet.generateCommonQuestionsBullet(context)[1])
                        ),
                        CommonQuestionsHeader(
                            header = context.getString(R.string.title_if_corporation),
                            listOf(CommonQuestionsBullet.generateCommonQuestionsBullet(context)[1])
                        ),
                    ),
                    expand = false
                ), CommonQuestions(
                    id = 2,
                    title = context.getString(R.string.title_what_are_the_loan_terms),
                    header = listOf(
                        CommonQuestionsHeader(
                            header = null,
                            listOf(CommonQuestionsBullet.generateCommonQuestionsBullet(context)[2])
                        )
                    ),
                    expand = false
                )
            )
        }
    }
}

@Serializable
data class  CommonQuestionsHeader(
    val header: String? = null,
    val bullet: List<CommonQuestionsBullet>? = null
)

@Serializable
data class CommonQuestionsBullet(
    val id: Int? = null,
    val bullet: List<String>? = null
) {
    companion object {
        fun generateCommonQuestionsBullet(context: Context): List<CommonQuestionsBullet> {
            return listOf(
                CommonQuestionsBullet(
                    id = 0,
                    listOf(
                        context.getString(R.string.bullet_must_be_a_filipino),
                        context.getString(R.string.bullet_must_be_eighteen),
                        context.getString(R.string.bullet_must_have_no_existing_business_loan)
                    )
                ),
                CommonQuestionsBullet(
                    id = 1,
                    listOf(
                        context.getString(R.string.bullet_valid_id),
                        context.getString(R.string.bullet_business_or_inventory_photo),
                        context.getString(R.string.bullet_one_month_issued),
                        context.getString(R.string.bullet_one_month_issued),
                    )
                ),
                CommonQuestionsBullet(
                    id = 2,
                    listOf(
                        context.getString(R.string.bullet_borrower_limit),
                        context.getString(R.string.bullet_term),
                        context.getString(R.string.bullet_collateral),
                        context.getString(R.string.bullet_availment),
                        context.getString(R.string.bullet_interest_rate),
                        context.getString(R.string.bullet_repayment)
                    )
                )
            )
        }
    }
}

@Serializable
data class CommonQuestionsSubBullet(
    val id: Int? = null,
    val title: String? = null
)