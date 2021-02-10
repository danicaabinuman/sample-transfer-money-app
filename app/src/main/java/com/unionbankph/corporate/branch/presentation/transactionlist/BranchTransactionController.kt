package com.unionbankph.corporate.branch.presentation.transactionlist

import android.content.Context
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.presentation.epoxymodel.branchTransactionItem
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper

class BranchTransactionController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed2EpoxyController<MutableList<BranchTransactionForm>, Boolean>() {

    private lateinit var callbacks: EpoxyAdapterCallback<BranchTransactionForm>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<BranchTransactionForm>, isSelection: Boolean) {
        data.forEachIndexed { position, branchTransactionForm ->
            branchTransactionItem {
                id(position)
                branchTransactionFormString(JsonHelper.toJson(branchTransactionForm))
                hasSelected(branchTransactionForm.isSelected)
                hasSelection(isSelection)
                callbacks(callbacks)
                context(context)
                autoFormatUtil(autoFormatUtil)
                viewUtil(viewUtil)
                position(position)
            }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<BranchTransactionForm>) {
        this.callbacks = callbacks
    }

}
