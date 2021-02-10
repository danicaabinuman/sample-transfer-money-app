package com.unionbankph.corporate.settings.presentation.single_selector

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.loadingFooter
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_selector.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import kotlinx.serialization.enumFromName
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.ThreadSafe

/**
 * Created by herald25santos on 2020-02-18
 */
class SingleSelectorActivity :
    BaseActivity<SingleSelectorViewModel>(R.layout.activity_selector),
    EpoxyAdapterCallback<Selector> {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolBar)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        imageViewClearText.setOnClickListener {
            editTextSearch.text?.clear()
        }
        initRxSearchEventListener()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[SingleSelectorViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        showLoading(
                            viewLoadingState,
                            srl_selector,
                            rv_selector,
                            textViewState
                        )
                        if (viewLoadingState.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    }
                }
                is UiState.Complete -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        dismissLoading(
                            viewLoadingState,
                            srl_selector,
                            rv_selector
                        )
                    }
                }
                is UiState.Exit -> {
                    onBackPressed()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.items.observe(this, Observer {
            updateController(it)
            if (viewModel.pageable.isInitialLoad) {
                showEmptyState(it)
                scrollToTop()
            }
        })
        viewModel.isPaginated.onNext(intent.getBooleanExtra(EXTRA_IS_PAGINATED, false))
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

    private fun init() {
        intent.getBooleanExtra(EXTRA_HAS_SEARCH, false).let {
            view_search_layout.visibility(it)
        }
    }

    private fun initBinding() {
        viewModel.setSelector(intent.getStringExtra(EXTRA_SELECTOR).notNullable())
        viewModel.hasAPIRequest
            .subscribe {
                if (it) {
                    srl_selector.apply {
                        setColorSchemeResources(getAccentColor())
                        setOnRefreshListener {
                            fetchSingleSelector(true)
                        }
                    }
                    val linearLayoutManager = getLinearLayoutManager()
                    rv_selector.layoutManager = linearLayoutManager
                    rv_selector.addOnScrollListener(paginationScrollListener(linearLayoutManager))
                } else {
                    srl_selector.apply {
                        setColorSchemeResources(getAccentColor())
                        isRefreshing = false
                        isEnabled = false
                    }
                }
            }.addTo(disposables)
        viewModel.selector
            .observeOn(schedulerProvider.ui())
            .subscribe {
                val singleSelectorTypeEnum = enumFromName(SingleSelectorTypeEnum::class, it)
                setToolbarTitle(tvToolbar, singleSelectorTypeEnum.value)
            }.addTo(disposables)
        fetchSingleSelector(true)
    }

    private fun paginationScrollListener(
        linearLayoutManager: LinearLayoutManager
    ): PaginationScrollListener {
        return object : PaginationScrollListener(linearLayoutManager) {
            override val totalPageCount: Int
                get() = viewModel.pageable.totalPageCount
            override val isLastPage: Boolean
                get() = viewModel.pageable.isLastPage
            override val isLoading: Boolean
                get() = viewModel.pageable.isLoadingPagination
            override val isFailed: Boolean
                get() = viewModel.pageable.isFailed

            override fun loadMoreItems() {
                if (!viewModel.pageable.isLoadingPagination
                    && viewModel.isPaginated.value == true
                ) {
                    fetchSingleSelector(false)
                }
            }
        }
    }

    private fun updateController(
        data: MutableList<Selector> = viewModel.items.value.notNullable()
    ) {
        rv_selector.withModels {
            data.forEachIndexed { index, selector ->
                singleSelectorItem {
                    id(selector.id)
                    item(selector)
                    callbacks(this@SingleSelectorActivity)
                    position(index)
                }
            }
            viewModel.isPaginated.value?.let {
                if (it && data.size > 0) {
                    loadingFooter {
                        id("loadingFooter")
                        loading(viewModel.pageable.isLoadingPagination)
                    }
                }
            }
        }
    }

    override fun onClickItem(view: View, data: Selector, position: Int) {
        viewModel.onClickedItem(position)
    }

    override fun onTapToRetry() {
        fetchSingleSelector(isInitialLoading = false, isTapToRetry = true)
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this)
                fetchSingleSelector(true)
                return@OnEditorActionListener true
            }
            false
        })
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
                    if (viewModel.isPaginated.value == true) {
                        viewModel.pageable.filter = filter.text().toString().nullable()
                        fetchSingleSelector(true)
                    } else {
                        viewModel.filterSearch(filter.text().toString())
                    }
                }
            }
            .addTo(disposables)
    }

    private fun showEmptyState(data: MutableList<Selector>) {
        if (data.size > 0) {
            if (textViewState?.visibility == View.VISIBLE) textViewState?.visibility = View.GONE
        } else {
            textViewState?.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        rv_selector.post {
            rv_selector.scrollToPosition(0)
        }
    }

    private fun fetchSingleSelector(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getSingleSelector(
            intent.getStringExtra(EXTRA_SELECTOR).notNullable(),
            isInitialLoading,
            intent.getStringExtra(EXTRA_PARAM)
        )
    }

    @ThreadSafe
    companion object {
        const val EXTRA_SELECTOR = "selector"
        const val EXTRA_IS_PAGINATED = "isPaginated"
        const val EXTRA_HAS_SEARCH = "hasSearch"
        const val EXTRA_PARAM = "param"
    }
}
