package com.unionbankph.corporate.approval.presentation.approval_cwt

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.approval.presentation.approval_cwt_detail.BatchCWTDetailActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.model.CWTDetail
import com.unionbankph.corporate.fund_transfer.data.model.CWTItem
import kotlinx.android.synthetic.main.activity_batch_cwt.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*


class BatchCWTActivity :
    BaseActivity<BatchCWTViewModel>(R.layout.activity_batch_cwt),
    EpoxyAdapterCallback<CWTItem> {

    private val pageable by lazyFast { Pageable() }

    private val referenceId by lazyFast { intent.getStringExtra(EXTRA_REFERENCE_ID) }

    private val type by lazyFast { intent.getStringExtra(EXTRA_TYPE) }

    private val controller by lazyFast {
        BatchCWTController(
            this,
            viewUtil
        )
    }

    private var cwt: MutableList<CWTItem> = mutableListOf()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(
            tvToolbar, formatString(
                if (type == BATCH_TYPE_CWT) {
                    R.string.title_creditable_withholding_tax
                } else {
                    R.string.title_additional_information
                }
            )
        )
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[BatchCWTViewModel::class.java]
        viewModel.batchStateLiveData.observe(this, Observer {
            when (it) {
                is ShowBatchCWTLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutCWT,
                        recyclerViewCWT,
                        textViewState
                    )
                }
                is ShowBatchCWTDismissLoading -> {
                    dismissLoading(viewLoadingState, swipeRefreshLayoutCWT, recyclerViewCWT)
                }
                is ShowBatchCWTDetailLoading -> {
                    viewHeaderLoadingState.visibility(true)
                }
                is ShowBatchCWTDetailDismissLoading -> {
                    viewHeaderLoadingState.visibility(false)
                }
                is ShowBatchCWTEndlessLoading -> {
                    pageable.isLoadingPagination = true
                    updateController()
                }
                is ShowBatchCWTDismissEndlessLoading -> {
                    pageable.isLoadingPagination = false
                    updateController()
                }
                is ShowBatchDetailCWTError -> {
                    scrollView.visibility(false)
                    handleOnError(it.throwable)
                }
                is ShowBatchCWTError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.cwtLiveData.observe(this, Observer {
            it?.let { cwtItems ->
                if (pageable.currentPage() == 1) {
                    cwt = cwtItems
                    if (pageable.isLastPage) {
                        if (cwt.size == 1) {
                            swipeRefreshLayoutCWT.visibility(false)
                            viewUtil.startAnimateView(true, scrollView, android.R.anim.fade_in)
                            viewModel.getFundTransferCWTHeader(type.notNullable())
                        } else {
                            showEmptyState()
                            updateController()
                        }
                    } else {
                        updateController()
                    }
                } else {
                    cwt.addAll(cwtItems)
                    updateController()
                }
            }
        })
        viewModel.cwtHeaderLiveData.observe(this, Observer {
            it?.let { cwtHeaders ->
                initCWTDetails(cwtHeaders)
            }
        })
        getBatchDetailCWT(true)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        swipeRefreshLayoutCWT.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getBatchDetailCWT(true)
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

    override fun onClickItem(view: View, data: CWTItem, position: Int) {
        val bundle = Bundle().apply {
            putString(
                BatchCWTDetailActivity.EXTRA_DATA,
                JsonHelper.toJson(data)
            )
            putString(BatchCWTDetailActivity.EXTRA_TYPE, type)
        }
        navigator.navigate(
            this,
            BatchCWTDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override fun onTapToRetry() {
        getBatchDetailCWT(isInitialLoading = false, isTapToRetry = true)
    }

    private fun initCWTDetails(cwtHeaders: MutableList<CWTDetail>) {
        scrollView.visibility(true)
        linearLayoutCWT.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        linearLayoutCWT.requestLayout()
        cwtHeaders.forEachIndexed { index, cwtHeader ->
            val view = layoutInflater.inflate(R.layout.item_cwt_detail, null)
            val textViewCWTTitle = view.findViewById<TextView>(R.id.textViewCWTTitle)
            val textViewCWT = view.findViewById<TextView>(R.id.textViewCWT)
            val viewBorderTop = view.findViewById<View>(R.id.viewBorderTop)
            val cwtItemFindValue = cwt[0].cwtBody.find { it.key == cwtHeader.key }
            viewBorderTop.visibility(index != 0)
            textViewCWTTitle.text = cwtHeader.display
            textViewCWT.text = cwtItemFindValue?.display.notEmpty()
            linearLayoutCWT.addView(view)
        }
    }

    private fun updateController() {
        controller.setData(cwt, pageable)
        if (pageable.isInitialLoad) scrollToTop()
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        recyclerViewCWT.layoutManager = linearLayoutManager
        recyclerViewCWT.addOnScrollListener(
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
                    if (!pageable.isLoadingPagination) getBatchDetailCWT(false)
                }
            }
        )
        recyclerViewCWT.setController(controller)
        controller.setEpoxyAdapterCallback(this)
    }

    private fun showEmptyState() {
        textViewState.text = formatString(
            if (type == BATCH_TYPE_CWT)
                R.string.title_no_creditable_withholding_tax
            else
                R.string.title_no_additional_information
        )
        if (cwt.size > 0) {
            if (textViewState?.visibility == View.VISIBLE) textViewState?.visibility = View.GONE
        } else {
            textViewState?.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        recyclerViewCWT.post {
            recyclerViewCWT.smoothScrollToPosition(0)
        }
    }

    private fun getBatchDetailCWT(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getFundTransferCWT(
            type.notNullable(),
            pageable,
            referenceId.notNullable(),
            isInitialLoading
        )
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_REFERENCE_ID = "reference_id"
    }

}
