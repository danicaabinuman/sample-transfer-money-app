package com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_done

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ManageScheduledTransferActivity
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ManageScheduledTransferController
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ManageScheduledTransferViewModel
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ShowManageScheduledTransferDismissLoading
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ShowManageScheduledTransferEndlessDismissLoading
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ShowManageScheduledTransferEndlessLoading
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ShowManageScheduledTransferError
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ShowManageScheduledTransferLoading
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_manage_scheduled_transfer_ongoing.*
import kotlinx.android.synthetic.main.widget_table_view.*
import timber.log.Timber

/**
 * Created by herald25santos on 12/03/2019
 */
class ManageScheduledTransferDoneFragment :
    BaseFragment<ManageScheduledTransferViewModel>(R.layout.fragment_manage_scheduled_transfer_ongoing),
    EpoxyAdapterCallback<Transaction> {

    private var snackBarProgressBar: Snackbar? = null

    private val pageable by lazyFast { Pageable() }

    private val controller by lazyFast {
        ManageScheduledTransferController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    override fun beforeLayout(savedInstanceState: Bundle?) {
        super.beforeLayout(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            getManageScheduledTransfers(true)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getManageScheduledTransfers(true)
            }
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_SCHEDULED_TRANSFER_LIST) {
                Timber.d("ACTION_UPDATE_SCHEDULED_TRANSFER_LIST")
                getSwipeRefreshLayout().isRefreshing = true
                getManageScheduledTransfers(true)
            }
        }.addTo(disposables)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[ManageScheduledTransferViewModel::class.java]
        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowManageScheduledTransferLoading -> {
                    showLoading(
                        viewLoadingState,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        textViewState
                    )
                }
                is ShowManageScheduledTransferDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowManageScheduledTransferEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowManageScheduledTransferEndlessDismissLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowManageScheduledTransferError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.transactions.observe(this, Observer {
            it?.let {
                updateController(it)
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        getManageScheduledTransfers(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuHelp = menu.findItem(R.id.menu_help)
        menuHelp.isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_help -> {
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_SCHEDULED_TRANSFER)
                )
                return true
            }
        }
        return false
    }

    override fun onClickItem(view: View, data: Transaction, position: Int) {
        val bundle = Bundle()
        bundle.putString(
            ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL,
            JsonHelper.toJson(data)
        )
        bundle.putBoolean(ApprovalDetailActivity.EXTRA_CANCEL_TRANSACTION, false)
        navigator.navigate(
            (activity as ManageScheduledTransferActivity),
            ApprovalDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    override fun onTapToRetry() {
        getManageScheduledTransfers(isInitialLoading = false, isTapToRetry = true)
    }

    private fun updateController(data: MutableList<Transaction> = getTransactionsLiveData()) {
        controller.setData(data, false, pageable, isTableView())
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = true
            if (isTableView()) {
                if (snackBarProgressBar == null) {
                    snackBarProgressBar = viewUtil.showCustomSnackBar(
                        constraintLayout,
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
            linearLayoutRow.visibility(data.isNotEmpty())
        }
        if (data.isNotEmpty()) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.text = getString(R.string.title_no_inactive_scheduled_transfer)
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun getManageScheduledTransfers(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getManageScheduledTransfers(
            ManageScheduledTransferViewModel.STATUS_DONE,
            pageable,
            isInitialLoading
        )
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        initHeaderRow()
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
                    if (!pageable.isLoadingPagination) getManageScheduledTransfers(false)
                }
            }
        )
        getRecyclerView().setController(controller)
        controller.setAdapterCallbacks(this)
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            swipeRefreshLayoutTable.visibility(true)
            swipeRefreshLayoutManageScheduledTransfer.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_manage_scheduled_transfer)
                    .toMutableList()
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                textViewHeader.text = it
                linearLayoutRow.addView(viewRowHeader)
            }
        }
    }

    private fun getTransactionsLiveData(): MutableList<Transaction> {
        return viewModel.transactions.value.notNullable()
    }

    private fun getRecyclerView() =
        if (isTableView()) recyclerViewTable else recyclerViewApprovalManageScheduledTransfer

    private fun getSwipeRefreshLayout() =
        if (isTableView()) swipeRefreshLayoutTable else swipeRefreshLayoutManageScheduledTransfer

    companion object {

        fun newInstance(): ManageScheduledTransferDoneFragment {
            val fragment =
                ManageScheduledTransferDoneFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
