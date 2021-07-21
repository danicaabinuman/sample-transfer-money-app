package com.unionbankph.corporate.account.presentation.account_selection

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityAccountSelectionBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class AccountSelectionActivity :
    BaseActivity<ActivityAccountSelectionBinding, AccountSelectionViewModel>(),
    AccountAdapterCallback {

    private val controller by lazyFast {
        AccountSelectionController(
            this,
            viewUtil,
            autoFormatUtil
        )
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initDataBus()
        initRxSearchEventListener()
        binding.swipeRefreshLayoutAccounts.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccounts(true)
            }
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowAccountSelectionLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        showLoading(
                            binding.viewLoadingState.root,
                            binding.swipeRefreshLayoutAccounts,
                            binding.recyclerViewAccounts,
                            binding.textViewState
                        )
                        if (binding.viewLoadingState.root.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    }
                }
                is ShowAccountSelectionDismissLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        dismissLoading(
                            binding.viewLoadingState.root,
                            binding.swipeRefreshLayoutAccounts,
                            binding.recyclerViewAccounts
                        )
                    }
                }
                is ShowAccountSelectionLoadingAccountDetail -> {
                    updateController()
                }
                is ShowAccountSelectionDismissLoadingAccountDetail -> {
                    updateController()
                }
                is ShowAccountSelectionDetailError,
                is ShowAccountsSelectionDetailError -> {
                    updateController()
                }
                is ShowAccountSelectionError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.accounts.observe(this, Observer {
            it?.let {
                updateController(it)
                if (viewModel.pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        getAccounts(true)
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

    private fun updateController(data: MutableList<Account> = viewModel.getAccounts()) {
        controller.setData(data, viewModel.pageable)
    }

    override fun onClickItem(account: String, position: Int) {
        eventBus.accountSyncEvent.emmit(
            BaseEvent(
                AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT,
                JsonHelper.fromJson(account)
            )
        )
        onBackPressed()
    }

    override fun onTapErrorRetry(id: String, position: Int) {
        viewModel.getCorporateUserAccountDetail(id, position)
    }

    override fun onTapToRetry() {
        getAccounts(isInitialLoading = false, isTapToRetry = true)
    }

    private fun initDataBus() {
        dataBus.accountDataBus.flowable.subscribe {
            viewModel.updateEachAccount(it)
        }.addTo(disposables)
    }

    private fun showEmptyState(data: MutableList<Account> = viewModel.getAccounts()) {
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun init() {
        initRecyclerView()
        intent.getStringExtra(EXTRA_PAGE)?.let {
            viewModel.page.onNext(it)
        }
    }

    private fun initBinding() {
        viewModel.page.subscribe {
            setToolbarTitle(
                binding.viewToolbar.tvToolbar,
                when (it) {
                    PAGE_BILLS_PAYMENT -> {
                        formatString(R.string.title_payment_from)
                    }
                    PAGE_CHECK_DEPOSIT_FILTER -> {
                        formatString(R.string.title_deposit_account)
                    }
                    PAGE_CHECK_DEPOSIT,
                    PAGE_BRANCH_VISIT,
                    PAGE_EBILLING -> {
                        formatString(R.string.title_deposit_to)
                    }
                    else -> {
                        formatString(R.string.title_transfer_from)
                    }
                }
            )

        }.addTo(disposables)
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewAccounts.layoutManager = linearLayoutManager
        binding.recyclerViewAccounts.addOnScrollListener(
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
                    if (!viewModel.pageable.isLoadingPagination) getAccounts(false)
                }
            }
        )
        controller.setAdapterCallbacks(this)
        binding.recyclerViewAccounts.setController(controller)
    }

    private fun scrollToTop() {
        binding.recyclerViewAccounts.post {
            binding.recyclerViewAccounts.scrollToPosition(0)
        }
    }

    private fun initRxSearchEventListener() {
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.viewSearchLayout.editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this@AccountSelectionActivity)
                getAccounts(true)
                return@OnEditorActionListener true
            }
            false
        })
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
                    viewModel.pageable.filter = filter.text().toString().nullable()
                    getAccounts(true)
                }
            }
            .addTo(disposables)
    }

    private fun getAccounts(
        isInitialLoading: Boolean,
        isTapToRetry: Boolean = false
    ) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getAccountsPermission(
            isInitialLoading,
            channelId = intent.getStringExtra(EXTRA_CHANNEL_ID),
            permissionId = intent.getStringExtra(EXTRA_PERMISSION_ID),
            destinationId = intent.getStringExtra(EXTRA_ID),
            exceptCurrency = intent.getStringExtra(EXTRA_EXCEPT_CURRENCY)
        )
    }

    companion object {
        const val PAGE_FUND_TRANSFER = "fund_transfer"
        const val PAGE_BILLS_PAYMENT = "bills_payment"
        const val PAGE_CHECK_DEPOSIT = "check_deposit"
        const val PAGE_CHECK_DEPOSIT_FILTER = "check_deposit_filter"
        const val PAGE_BRANCH_VISIT = "branch_visit"
        const val PAGE_EBILLING = "ebilling"

        const val EXTRA_PAGE = "page"
        const val EXTRA_ID = "id"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_PERMISSION_ID = "permission_id"
        const val EXTRA_EXCEPT_CURRENCY = "except_currency"
    }

    override val layoutId: Int
        get() = R.layout.activity_account_selection
    override val viewModelClassType: Class<AccountSelectionViewModel>
        get() = AccountSelectionViewModel::class.java
}
