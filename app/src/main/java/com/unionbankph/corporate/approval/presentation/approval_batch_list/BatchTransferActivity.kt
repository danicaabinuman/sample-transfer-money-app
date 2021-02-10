package com.unionbankph.corporate.approval.presentation.approval_batch_list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.approval.presentation.approval_batch_detail.BatchDetailActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.model.Batch
import kotlinx.android.synthetic.main.activity_batch_transfer.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*


class BatchTransferActivity :
    BaseActivity<BatchTransferViewModel>(R.layout.activity_batch_transfer),
    EpoxyAdapterCallback<Batch> {

    private val pageable by lazyFast { Pageable() }

    private val supportCWT by lazyFast { intent.getBooleanExtra(EXTRA_SUPPORT_CWT, false) }

    private val controller by lazyFast {
        BatchTransferController(
            this,
            viewUtil,
            autoFormatUtil
        )
    }

    private val id by lazyFast { intent.getStringExtra(EXTRA_ID) }

    private var batchTransfers: MutableList<Batch> = mutableListOf()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.title_view_all_transfers))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[BatchTransferViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowBatchTransferLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutBatchTransfer,
                        recyclerViewBatchTransfer,
                        textViewState
                    )
                }
                is ShowBatchTransferDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutBatchTransfer,
                        recyclerViewBatchTransfer
                    )
                }
                is ShowBatchTransferEndlessLoading -> {
                    pageable.isLoadingPagination = true
                    updateController()
                }
                is ShowBatchTransferEndlessDismissLoading -> {
                    pageable.isLoadingPagination = false
                    updateController()
                }
                is ShowBatchTransferError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.batchTransferLiveDate.observe(this, Observer {
            it?.let { pagedDto ->
                pageable.apply {
                    totalPageCount = pagedDto.totalPages
                    isLastPage = !pagedDto.hasNextPage
                    increasePage()
                }
                if (pagedDto.currentPage == 0) {
                    batchTransfers = pagedDto.results
                    showEmptyState()
                    scrollToTop()
                } else {
                    batchTransfers.addAll(pagedDto.results)
                }
                updateController()
            }
        })
        getBatchTransfers(true)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        swipeRefreshLayoutBatchTransfer.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getBatchTransfers(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onClickItem(view: View, data: Batch, position: Int) {
        val bundle = Bundle().apply {
            putString(
                BatchDetailActivity.EXTRA_BATCH,
                JsonHelper.toJson(data)
            )
            putBoolean(BatchDetailActivity.EXTRA_SUPPORT_CWT, supportCWT)
        }
        navigator.navigate(
            this,
            BatchDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override fun onTapToRetry() {
        getBatchTransfers(isInitialLoading = false, isTapToRetry = true)
    }

    private fun updateController() {
        controller.setData(batchTransfers, pageable)
    }

    private fun showEmptyState() {
        if (batchTransfers.size > 0) {
            if (textViewState?.visibility == View.VISIBLE) textViewState?.visibility = View.GONE
        } else {
            textViewState?.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        recyclerViewBatchTransfer.layoutManager = linearLayoutManager
        recyclerViewBatchTransfer.addOnScrollListener(
            object : PaginationScrollListener(linearLayoutManager) {
                override val totalPageCount: Int
                    get() = pageable.totalPageCount
                override val isLastPage: Boolean
                    get() = pageable.isLastPage
                override val isLoading: Boolean
                    get() = pageable.isLoadingPagination
                override val isFailed: Boolean
                    get() = pageable.isFailed

                override fun loadMoreItems() {
                    if (!pageable.isLoadingPagination) getBatchTransfers(false)
                }
            }
        )
        recyclerViewBatchTransfer.setController(controller)
        controller.setEpoxyAdapterCallback(this)
    }

    private fun scrollToTop() {
        recyclerViewBatchTransfer.post {
            recyclerViewBatchTransfer.smoothScrollToPosition(0)
        }
    }

    private fun getBatchTransfers(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getBatchTransaction(pageable, id, isInitialLoading)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_SUPPORT_CWT = "support_cwt"
    }

}
