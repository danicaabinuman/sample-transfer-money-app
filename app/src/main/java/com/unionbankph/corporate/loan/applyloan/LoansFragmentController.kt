package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.Typed2EpoxyController
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.*
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable

class LoansFragmentController
constructor(
    private val context: Context,
) : TypedEpoxyController<LoansViewState>() {

    @AutoModel
    lateinit var loansHeaderItemModel: LoansHeaderItemModel_

    @AutoModel
    lateinit var keyFeaturesItemModel: KeyFeaturesMainModel_

    @AutoModel
    lateinit var financeWithUsItemModel: FinanceWithUsMainModel_

    @AutoModel
    lateinit var loanTermsMainModel: LoanTermsMainModel_

    @AutoModel
    lateinit var eligibleMainModel: EligibleMainModel_

    @AutoModel
    lateinit var commonQuestionsMainModel: CommonQuestionsMainModel_

    @AutoModel
    lateinit var readyToBusinessMainModel: ReadyToBusinessMainModel_

    private lateinit var loansAdapterCallback: LoansAdapterCallback

    var commonQuestionListener: (CommonQuestions) -> Unit = { _ -> }

    var applyLoansListener: (View) -> Unit = { }

    var readyToBusiness: (View) -> Unit = { }

    init {

    }

    override fun buildModels(loansViewState: LoansViewState) {

        loansViewState.let {

            loansHeaderItemModel
                .callbacks(loansAdapterCallback)
                .applyLoanListener { applyLoansListener(it) }
                .addTo(this)

            keyFeaturesItemModel
                .context(context)
                .callbacks(loansAdapterCallback)
                .addTo(this)

            financeWithUsItemModel
                .context(context)
                .callbacks(loansAdapterCallback)
                .addTo(this)

            /*loanTermsMainModel
                .context(context)
                .callbacks(loansAdapterCallback)
                .addTo(this)*/

            eligibleMainModel
                .context(context)
                .callbacks(loansAdapterCallback)
                .addTo(this)

/*        commonQuestionsMainModel
            .context(context)
            .dataFromViewModel(loansViewState.commonQuestions)
            .clickListener { commonQuestionListener(it) }
            .addTo(this)*/

            commonQuestionEligible {
                id("common-question-eligible")
                expand(it.commonQuestionEligible)
                onClickListener { model, parentView, clickedView, position ->
                    this@LoansFragmentController.loansAdapterCallback.onCommonQuestionsEligible(
                        loansViewState.commonQuestionEligible
                    )
                }
            }

            commonQuestionRequirements {
                id("common-question-requirements")
                expand(it.commonQuestionRequirement)
                onClickListener { model, parentView, clickedView, position ->
                    this@LoansFragmentController.loansAdapterCallback.onCommonQuestionRequirements(
                        loansViewState.commonQuestionRequirement
                    )
                }
            }

            commonQuestionBusinessLoan {
                id("common-question-business")
                expand(it.commonQuestionBusiness)
                onClickListener { model, parentView, clickedView, position ->
                    this@LoansFragmentController.loansAdapterCallback.oncCommonQuestionBusiness(
                        loansViewState.commonQuestionBusiness
                    )
                }
            }

            readyToBusinessMainModel
                .context(context)
                .clickListener { readyToBusiness(it) }
                .addTo(this)
        }
    }

    fun setLoansHeaderAdapterCallback(loansAdapterCallback: LoansAdapterCallback) {
        this.loansAdapterCallback = loansAdapterCallback
    }

}



