package com.unionbankph.corporate.branch.presentation.list

import android.content.Context
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.branch.presentation.epoxymodel.branchVisitItem
import com.unionbankph.corporate.branch.presentation.epoxymodel.branchVisitRow
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback

class BranchVisitController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed3EpoxyController<MutableList<BranchVisit>, Pageable, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<BranchVisit>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        data: MutableList<BranchVisit>,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        data.forEachIndexed { position, branchVisit ->
            if (isTableView) {
                branchVisitRow {
                    id(branchVisit.id)
                    branchVisit(branchVisit)
                    callbacks(this@BranchVisitController.callbacks)
                    context(this@BranchVisitController.context)
                    viewUtil(this@BranchVisitController.viewUtil)
                    position(position)
                }
            } else {
                branchVisitItem {
                    id(branchVisit.id)
                    branchVisit(branchVisit)
                    callbacks(this@BranchVisitController.callbacks)
                    context(this@BranchVisitController.context)
                    viewUtil(this@BranchVisitController.viewUtil)
                    position(position)
                }
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination && !isTableView, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<BranchVisit>) {
        this.callbacks = callbacks
    }
}
