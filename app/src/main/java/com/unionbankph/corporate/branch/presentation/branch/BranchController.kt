package com.unionbankph.corporate.branch.presentation.branch

import android.content.Context
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.presentation.epoxymodel.branchItem
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback

class BranchController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : TypedEpoxyController<MutableList<Branch>>() {

    private lateinit var callbacks: EpoxyAdapterCallback<Branch>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<Branch>) {
        data.forEachIndexed { position, branch ->
            branchItem {
                id(branch.id)
                branch(branch)
                callbacks(callbacks)
                context(context)
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

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<Branch>) {
        this.callbacks = callbacks
    }
}
