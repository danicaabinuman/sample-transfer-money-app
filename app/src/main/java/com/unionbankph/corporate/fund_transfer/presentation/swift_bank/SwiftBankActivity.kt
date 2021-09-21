package com.unionbankph.corporate.fund_transfer.presentation.swift_bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivitySwiftBankBinding
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


class SwiftBankActivity :
    BaseActivity<ActivitySwiftBankBinding, SwiftBankViewModel>(),
    EpoxyAdapterCallback<SwiftBank> {

    private val controller by lazyFast {
        SwiftBankController(
            this,
            viewUtil
        )
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.hint_select_receiving_bank))
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
                            binding.viewLoadingState.root,
                            binding.swipeRefreshLayoutSwiftBanks,
                            binding.recyclerViewSwiftBanks,
                            binding.textViewState
                        )
                        if (binding.viewLoadingState.root.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    } else {
                        updateController()
                    }

                }
                is UiState.Complete -> {
                    if (viewModel.pageable.isInitialLoad) {
                        dismissLoading(
                            binding.viewLoadingState.root,
                            binding.swipeRefreshLayoutSwiftBanks,
                            binding.recyclerViewSwiftBanks
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
        viewModel.swiftBanks.observe(this, Observer {
            it?.let {
                updateController(it)
                if (viewModel.pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        getSwiftBanks(true)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initRxSearchEventListener()
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        binding.swipeRefreshLayoutSwiftBanks.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getSwiftBanks(true)
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

    override fun onClickItem(view: View, data: SwiftBank, position: Int) {
        eventBus.inputSyncEvent.emmit(
            BaseEvent(InputSyncEvent.ACTION_INPUT_BANK_RECEIVER, JsonHelper.toJson(data))
        )
        onBackPressed()
    }

    override fun onTapToRetry() {
        getSwiftBanks(isInitialLoading = false, isTapToRetry = true)
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    binding.viewSearchLayout.editTextSearch.clearFocus()
                    getSwiftBanks(true)
                    viewUtil.dismissKeyboard(this@SwiftBankActivity)
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
                    viewModel.pageable.filter = filter.text().toString().nullable()
                    getSwiftBanks(true)
                }
            }.addTo(disposables)
    }

    private fun updateController(data: MutableList<SwiftBank> = viewModel.swiftBanks.value.notNullable()) {
        controller.setData(data, viewModel.pageable)
    }

    private fun showEmptyState(data: MutableList<SwiftBank>) {
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE)
                binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        binding.recyclerViewSwiftBanks.post {
            binding.recyclerViewSwiftBanks.smoothScrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewSwiftBanks.layoutManager = linearLayoutManager
        binding.recyclerViewSwiftBanks.addOnScrollListener(
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
                    if (!viewModel.pageable.isLoadingPagination) getSwiftBanks(false)
                }
            }
        )
        binding.recyclerViewSwiftBanks.setController(controller)
        controller.setEpoxyAdapterCallback(this)
    }

    private fun getSwiftBanks(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getSwiftBanks(isInitialLoading)
    }

    override val viewModelClassType: Class<SwiftBankViewModel>
        get() = SwiftBankViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySwiftBankBinding
        get() = ActivitySwiftBankBinding::inflate

}
