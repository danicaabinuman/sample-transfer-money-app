package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import com.airbnb.epoxy.*
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

    /*@AutoModel
    lateinit var longTermsItemModel: LongTermsItemModel_*/

    private lateinit var loansAdapterCallback: LoansAdapterCallback

    init {

    }

    override fun buildModels(loansViewState: LoansViewState, pageable: Pageable) {

        loansHeaderItemModel
            .callbacks(loansAdapterCallback)
            .addTo(this)

        keyFeaturesItemModel
            .context(context)
            .callbacks(loansAdapterCallback)
            .addTo(this)

        financeWithUsItemModel
            .context(context)
            .callbacks(loansAdapterCallback)
            .addTo(this)

    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setLoansHeaderAdapterCallback(loansAdapterCallback: LoansAdapterCallback) {
        this.loansAdapterCallback = loansAdapterCallback
    }


}



