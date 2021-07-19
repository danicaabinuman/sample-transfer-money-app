package com.unionbankph.corporate.approval.presentation.approval_done

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.ApprovalFragment
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentApprovalDoneBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


class ApprovalDoneFragment :
    BaseFragment<FragmentApprovalDoneBinding, ApprovalDoneViewModel>(),
    EpoxyAdapterCallback<Transaction> {

    private val pageable by lazyFast { Pageable() }

    private var snackBarProgressBar: Snackbar? = null

    private val controller by lazyFast {
        ApprovalDoneController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowApprovalDoneLoading -> {
                    showLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        binding.textViewState,
                        binding.viewTable.linearLayoutRow
                    )
                    if (binding.viewLoadingState.viewLoadingLayout.visibility == View.VISIBLE) {
                        updateController(mutableListOf())
                    }
                }
                is ShowApprovalDoneDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowApprovalDoneEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowApprovalDoneDismissEndlessLoading -> {
                    dismissEndlessProgressBar()
                }
            }
            if ((activity as DashboardActivity).viewPager().currentItem == 2
                && (parentFragment as ApprovalFragment).viewPager().currentItem == 1
            ) {
                when (it) {
                    is ShowApprovalDoneError -> {
                        handleOnError(it.throwable)
                    }
                }
            }
        })
        viewModel.transactions.observe(this, Observer {
            it?.let {
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
                updateController(it)
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            fetchApprovalDone(true)
        }
        (activity as DashboardActivity).allowMultipleSelectionApprovals(false)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initRxSearchEventListener()
        initSwipeRefreshLayout()
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.requestFocus()
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_SCROLL_TO_TOP ->
                    if ((activity as DashboardActivity).viewPager().currentItem == 2) {
                        getRecyclerView().smoothScrollToPosition(0)
                    }
                SettingsSyncEvent.ACTION_RESET_DEMO -> {
                    if (!hasInitialLoad) {
                        getSwipeRefreshLayout().isRefreshing = true
                        fetchApprovalDone(true)
                    }
                }
            }
        }.addTo(disposables)
        dataBus.approvalBatchIdDataBus.flowable.subscribe {
            getSwipeRefreshLayout().isRefreshing = true
            fetchApprovalDone(true)
        }.addTo(disposables)
        eventBus.actionSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_REFRESH_SCROLL_TO_TOP -> {
                    pageable.filter = null
                    getSwipeRefreshLayout().isRefreshing = true
                    fetchApprovalDone(true)
                }
                ActionSyncEvent.ACTION_RESET_LIST_UI -> {
                    resetUIList()
                }
            }
        }.addTo(disposables)
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    binding.viewSearchLayout.editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(getAppCompatActivity())
                    fetchApprovalDone(true)
                    return@OnEditorActionListener true
                }
                false
            }
        )
        RxTextView.textChangeEvents(binding.viewSearchLayout.editTextSearch)
            .debounce(
                resources.getInteger(R.integer.time_edit_text_search_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { filter ->
                binding.viewSearchLayout.imageViewClearText.visibility(filter.text().isNotEmpty())
                if (filter.view().isFocused) {
                    pageable.filter = filter.text().toString().nullable()
                    fetchApprovalDone(true)
                }
            }.addTo(disposables)
    }

    override fun onClickItem(view: View, data: Transaction, position: Int) {
        val bundle = Bundle().apply {
            putString(
                ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL,
                JsonHelper.toJson(data)
            )
        }
        navigator.navigate(
            getAppCompatActivity(),
            ApprovalDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    override fun onTapToRetry() {
        fetchApprovalDone(isInitialLoading = false, isTapToRetry = true)
    }

    private fun fetchApprovalDone(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getApprovalList(
            status = STATUS_DONE,
            pageable = pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun initRecyclerView() {
        initHeaderRow()
        controller.setAdapterCallbacks(this)
        val linearLayoutManager = getLinearLayoutManager()
        getRecyclerView().layoutManager = linearLayoutManager
        getRecyclerView().addOnScrollListener(
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
                    if (!pageable.isLoadingPagination) fetchApprovalDone(false)
                }
            }
        )
        getRecyclerView().setController(controller)
    }

    private fun resetUIList() {
        clearRecyclerView()
        initSwipeRefreshLayout()
        initRecyclerView()
        showEmptyState(getTransactions())
        scrollToTop()
        updateController()
    }

    private fun initSwipeRefreshLayout() {
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchApprovalDone(true)
            }
        }
    }

    private fun clearRecyclerView() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutApprovalDone.visibility(false)
            binding.recyclerViewApprovalDone.clear()
            binding.recyclerViewApprovalDone.clearPreloaders()
            binding.recyclerViewApprovalDone.adapter = null
            binding.recyclerViewApprovalDone.layoutManager = null
        } else {
            binding.swipeRefreshLayoutTable.visibility(false)
            binding.swipeRefreshLayoutApprovalDone.visibility(true)
            binding.viewTable.recyclerViewTable.clear()
            binding.viewTable.recyclerViewTable.clearPreloaders()
            binding.viewTable.recyclerViewTable.adapter = null
            binding.viewTable.recyclerViewTable.layoutManager = null
        }
    }

    private fun updateController(data: MutableList<Transaction> = getTransactions()) {
        controller.setData(data, pageable, isTableView())
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = true
            if (isTableView()) {
                if (snackBarProgressBar == null) {
                    snackBarProgressBar = viewUtil.showCustomSnackBar(
                        binding.coordinatorLayoutSnackBar,
                        R.layout.widget_snackbar_progressbar,
                        Snackbar.LENGTH_INDEFINITE
                    )
                }
                snackBarProgressBar?.show()
            } else {
                updateController()
            }
        }
    }

    private fun dismissEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = false
            if (isTableView()) {
                snackBarProgressBar?.dismiss()
            } else {
                updateController()
            }
        }
    }

    private fun showEmptyState(data: MutableList<Transaction>) {
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(data.isNotEmpty())
        }
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.coordinatorLayoutSnackBar.visibility(true)
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutApprovalDone.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_done_approvals).toMutableList()
            binding.viewTable.linearLayoutRow.removeAllViews()
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                textViewHeader.text = it
                binding.viewTable.linearLayoutRow.addView(viewRowHeader)
            }
        }
    }

    private fun getRecyclerView() =
        if (isTableView()) binding.viewTable.recyclerViewTable else binding.recyclerViewApprovalDone

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutApprovalDone

    private fun getTransactions(): MutableList<Transaction> =
        viewModel.transactions.value ?: mutableListOf()

    companion object {

        const val STATUS_DONE = "DONE"

        fun newInstance(): ApprovalDoneFragment {
            val fragment =
                ApprovalDoneFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }

    }

    override val layoutId: Int
        get() = R.layout.fragment_approval_done

    override val viewModelClassType: Class<ApprovalDoneViewModel>
        get() = ApprovalDoneViewModel::class.java

}
