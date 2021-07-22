package com.unionbankph.corporate.account.presentation.source_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivitySourceAccountSelectionBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class SourceAccountActivity :
    BaseActivity<ActivitySourceAccountSelectionBinding, SourceAccountViewModel>(),
    AccountAdapterCallback {

    private val controller by lazyFast {
        SourceAccountController(applicationContext, viewUtil)
    }

    private var menuCheck: MenuItem? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
        init()
    }

    private fun init() {
        binding.swipeRefreshLayoutSourceAccounts.isEnabled = false
        if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_DETAIL ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_DETAIL
        ) {
            setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_allowed_source_accounts))
            intent.getParcelableExtra<SourceAccountForm>(EXTRA_SOURCE_ACCOUNT_FORM)?.let {
                viewModel.setBeneficiaryAccounts(it.selectedAccounts)
            }
            updateController()
        } else {
            setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_source_account))
            intent.getParcelableExtra<SourceAccountForm>(EXTRA_SOURCE_ACCOUNT_FORM)?.let {
                viewModel.onSetSourceAccountForm(it)
            }
            getAccounts(true)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSourceAccountLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        showLoading(
                            binding.viewLoadingState.viewLoadingLayout,
                            binding.swipeRefreshLayoutSourceAccounts,
                            binding.recyclerViewSourceAccounts,
                            binding.textViewState
                        )
                        if (binding.viewLoadingState.viewLoadingLayout.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    }
                }
                is ShowSourceAccountDismissLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        dismissLoading(
                            binding.viewLoadingState.viewLoadingLayout,
                            binding.swipeRefreshLayoutSourceAccounts,
                            binding.recyclerViewSourceAccounts
                        )
                    }
                }
                is OnUpdateSelectionOfAccount -> {
                    if (it.isAllSelected) {
                        menuCheck?.title = getString(R.string.action_deselect_all)
                    } else {
                        menuCheck?.title = getString(R.string.action_select_all)
                    }
                    updateSelectButton(it.hasSelectedAccounts, it.countSelectedAccounts)
                    updateController()
                }
                is OnClickSelectAccounts -> {
                    eventBus.inputSyncEvent.emmit(
                        BaseEvent(
                            InputSyncEvent.ACTION_INPUT_SOURCE_ACCOUNTS,
                            it.sourceAccounts
                        )
                    )
                    onBackPressed()
                }
                is ShowSourceAccountError -> {
                    this.handleOnError(it.throwable)
                }
            }
        })
        viewModel.accounts.observe(this, Observer {
            it?.let {
                updateController(it)
                if (viewModel.pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                    val totalElements = viewModel.totalSelected
                    updateSelectButton(
                        totalElements > 0,
                        totalElements
                    )
                }
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initRxSearchEventListener()
        binding.swipeRefreshLayoutSourceAccounts.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccounts(true)
            }
        }
        RxView.clicks(binding.buttonSelectAccounts)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewModel.onChooseSelectedAccounts()
            }
            .addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val menuItemView = binding.viewToolbar.toolbar.findViewById<View>(R.id.menu_check)
        if (menuItemView is TextView) {
            menuItemView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
        }
        menu.findItem(R.id.menu_help)?.isVisible = false
        menuCheck = menu.findItem(R.id.menu_check)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_check -> {
                if (menuCheck?.title == getString(R.string.action_select_all)) {
                    viewModel.selectAllAccounts(true)
                } else {
                    viewModel.selectAllAccounts(false)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewSourceAccounts.layoutManager = linearLayoutManager
        binding.recyclerViewSourceAccounts.addOnScrollListener(
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
                    if (!viewModel.pageable.isLoadingPagination &&
                        intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_FORM ||
                        intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_FORM
                    ) {
                        getAccounts(false)
                    }
                }
            }
        )
        controller.setAccountAdapterCallback(this)
        binding.recyclerViewSourceAccounts.setController(controller)
    }

    private fun updateController(data: MutableList<Account> = viewModel.getAccounts()) {
        controller.setData(data, viewModel.pageable)
    }

    override fun onClickItem(account: String, position: Int) {
        val item = JsonHelper.fromJson<Account>(account)
        viewModel.onSelectAccount(position, item.isSelected)
    }

    override fun onTapToRetry() {
        getAccounts(isInitialLoading = false)
    }

    private fun getCurrentSelectedAccounts(): MutableList<Account>? {
        return intent.getParcelableArrayListExtra<Account>(EXTRA_SELECTED_LIST)?.toMutableList()
    }

    private fun updateSelectButton(isShownButton: Boolean, countSelectedAccounts: Int) {
        if (isShownButton) {
            binding.buttonSelectAccounts.visibility = View.VISIBLE
        } else {
            binding.buttonSelectAccounts.visibility = View.GONE
        }
        binding.buttonSelectAccounts.text = String.format(
            getString(
                R.string.params_select_accounts,
                countSelectedAccounts.toString()
            )
        )
    }

    private fun showEmptyState(data: MutableList<Account> = viewModel.getAccounts()) {
        if (data.size > 0) {
            menuCheck?.isVisible = intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_FORM ||
                    intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_FORM
            menuCheck?.title =
                if (viewModel.isSelectedAllAccounts()) {
                    getString(R.string.action_deselect_all)
                } else {
                    getString(R.string.action_select_all)
                }
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            menuCheck?.isVisible = false
            binding.textViewState.visibility = View.VISIBLE
            binding.buttonSelectAccounts.visibility = View.GONE
        }
    }

    private fun getPermissionByPage(): String {
        return if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_FORM ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_DETAIL
        ) "25"
        else "62"
    }

    private fun getChannelId(): String {
        return if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_FORM ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_DETAIL
        )
            intent.getStringExtra(EXTRA_CHANNEL_ID).notNullable()
        else ChannelBankEnum.BILLS_PAYMENT.getChannelId().toString()
    }


    private fun scrollToTop() {
        binding.recyclerViewSourceAccounts.post {
            binding.recyclerViewSourceAccounts.scrollToPosition(0)
        }
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.constraintLayoutSearch.isVisible = intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_FORM ||
                intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_FORM
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
                viewUtil.dismissKeyboard(this@SourceAccountActivity)
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
        viewModel.getSourceAccounts(
            isInitialLoading,
            getChannelId(),
            getPermissionByPage(),
            getCurrentSelectedAccounts()
        )
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val EXTRA_SELECTED_LIST = "selected_list"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_SOURCE_ACCOUNT_FORM = "source_account_form"
        const val PAGE_BENEFICIARY_DETAIL = "beneficiary_detail"
        const val PAGE_BENEFICIARY_FORM = "beneficiary_form"
        const val PAGE_FREQUENT_BILLER_FORM = "frequent_biller_form"
        const val PAGE_FREQUENT_BILLER_DETAIL = "frequent_biller_detail"
    }

    override val viewModelClassType: Class<SourceAccountViewModel>
        get() = SourceAccountViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySourceAccountSelectionBinding
        get() = ActivitySourceAccountSelectionBinding::inflate
}
