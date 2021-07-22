package com.unionbankph.corporate.account.presentation.account_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.model.SectionedAccountTransactionHistory
import com.unionbankph.corporate.account.presentation.account_history_detail.AccountTransactionHistoryDetailsActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityAccountTransactionHistoryBinding

class AccountTransactionHistoryActivity :
    BaseActivity<ActivityAccountTransactionHistoryBinding, AccountTransactionHistoryViewModel>(),
    EpoxyAdapterCallback<Record> {

    private val controller = AccountTransactionHistoryController()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_transaction_history))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (viewModel.pageable.isInitialLoad) {
                        showLoading(
                            binding.viewLoadingState.viewLoadingLayout,
                            binding.swipeRefreshLayoutTransactionHistory,
                            binding.recyclerViewTransactionHistory,
                            binding.textViewState
                        )
                    } else {
                        updateController()
                    }
                }
                is UiState.Complete -> {
                    if (viewModel.pageable.isInitialLoad) {
                        dismissLoading(
                            binding.viewLoadingState.viewLoadingLayout,
                            binding.swipeRefreshLayoutTransactionHistory,
                            binding.recyclerViewTransactionHistory
                        )
                    } else {
                        updateController()
                    }
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.sectionedTransactions.observe(this, Observer {
            it?.let {
                if (viewModel.pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
                updateController(it)
            }
        })
        getRecentTransactions(true)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.swipeRefreshLayoutTransactionHistory.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getRecentTransactions(true)
            }
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
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

    private fun updateController(
        data: MutableList<SectionedAccountTransactionHistory> = viewModel.sectionedTransactions.value.notNullable()
    ) {
        controller.setData(data, viewModel.pageable)
    }

    override fun onClickItem(view: View, data: Record, position: Int) {
        val bundle = Bundle()
        bundle.putString(
            AccountTransactionHistoryDetailsActivity.EXTRA_RECORD,
            JsonHelper.toJson(data)
        )
        bundle.putString(
            AccountTransactionHistoryDetailsActivity.EXTRA_ID,
            intent.getStringExtra(EXTRA_ID)
        )
        navigator.navigate(
            this,
            AccountTransactionHistoryDetailsActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun showEmptyState(data: MutableList<SectionedAccountTransactionHistory>) {
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        binding.recyclerViewTransactionHistory.post {
            binding.recyclerViewTransactionHistory.smoothScrollToPosition(0)
        }
    }

    override fun onTapToRetry() {
        getRecentTransactions(isInitialLoading = false, isTapToRetry = true)
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewTransactionHistory.layoutManager = linearLayoutManager
        binding.recyclerViewTransactionHistory.addOnScrollListener(
            object : PaginationScrollListener(linearLayoutManager) {
                override val totalPageCount: Int
                    get() = viewModel.pageable.totalPageCount
                override val isLastPage: Boolean
                    get() = viewModel.pageable.isLastPage
                override val isLoading: Boolean
                    get() = viewModel.pageable.isLoadingPagination
                override val isFailed: Boolean
                    get() = viewModel.pageable.isFailed

                override fun loadMoreItems() {
                    if (!viewModel.pageable.isLoadingPagination) getRecentTransactions(false)
                }
            }
        )
        binding.recyclerViewTransactionHistory.setController(controller)
    }

    private fun getRecentTransactions(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
            viewModel.lastItemRecord.onNext(Record())
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getRecentTransactions(
            intent.getStringExtra(EXTRA_ID).notNullable(),
            isInitialLoading
        )
    }

    companion object {
        const val EXTRA_ID = "id"
    }

    override val viewModelClassType: Class<AccountTransactionHistoryViewModel>
        get() = AccountTransactionHistoryViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityAccountTransactionHistoryBinding
        get() = ActivityAccountTransactionHistoryBinding::inflate
}
