package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable

class LoansFragmentController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed2EpoxyController<LoansViewState, Pageable>() {

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

    init {

    }

    override fun buildModels(loansViewState: LoansViewState, pageable: Pageable) {

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

        loanTermsMainModel
            .context(context)
            .callbacks(loansAdapterCallback)
            .addTo(this)

        eligibleMainModel
            .context(context)
            .callbacks(loansAdapterCallback)
            .addTo(this)

        commonQuestionsMainModel
            .context(context)
            .dataFromViewModel(loansViewState.commonQuestions)
            .clickListener { commonQuestionListener(it) }
            .addTo(this)

        readyToBusinessMainModel
            .context(context)
            .addTo(this)

/*        commonQuestionsMain {
            id("test")
            dataFromViewModel(loansViewState.commonQuestions)
            this.callbacks(this@LoansFragmentController.loansAdapterCallback)
        }*/
    }

/*    fun submitDataToCommonQuestions() {
        requestModelBuild()
    }*/

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setLoansHeaderAdapterCallback(loansAdapterCallback: LoansAdapterCallback) {
        this.loansAdapterCallback = loansAdapterCallback
    }


}



