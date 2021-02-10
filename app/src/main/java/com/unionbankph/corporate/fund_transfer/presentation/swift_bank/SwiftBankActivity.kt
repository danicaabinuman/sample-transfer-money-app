package com.unionbankph.corporate.fund_transfer.presentation.swift_bank

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
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_swift_bank.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.concurrent.TimeUnit


class SwiftBankActivity :
    BaseActivity<SwiftBankViewModel>(R.layout.activity_swift_bank),
    EpoxyAdapterCallback<SwiftBank> {

    private val controller by lazyFast {
        SwiftBankController(
            this,
            viewUtil
        )
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.hint_select_receiving_bank))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SwiftBankViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (viewModel.pageable.isInitialLoad) {
                        showLoading(
                            viewLoadingState,
                            swipeRefreshLayoutSwiftBanks,
                            recyclerViewSwiftBanks,
                            textViewState
                        )
                        if (viewLoadingState.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    } else {
                        updateController()
                    }

                }
                is UiState.Complete -> {
                    if (viewModel.pageable.isInitialLoad) {
                        dismissLoading(
                            viewLoadingState,
                            swipeRefreshLayoutSwiftBanks,
                            recyclerViewSwiftBanks
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
        RxView.clicks(imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                editTextSearch.text?.clear()
            }.addTo(disposables)
        swipeRefreshLayoutSwiftBanks.apply {
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
        editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    editTextSearch.clearFocus()
                    getSwiftBanks(true)
                    viewUtil.dismissKeyboard(this@SwiftBankActivity)
                    return@OnEditorActionListener true
                }
                false
            }
        )
        RxTextView.textChangeEvents(editTextSearch)
            .debounce(
                resources.getInteger(R.integer.time_edit_text_search_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { filter ->
                imageViewClearText.visibility(filter.text().isNotEmpty())
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
            if (textViewState?.visibility == View.VISIBLE) textViewState?.visibility = View.GONE
        } else {
            textViewState?.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        recyclerViewSwiftBanks.post {
            recyclerViewSwiftBanks.smoothScrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        recyclerViewSwiftBanks.layoutManager = linearLayoutManager
        recyclerViewSwiftBanks.addOnScrollListener(
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
        recyclerViewSwiftBanks.setController(controller)
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

}
