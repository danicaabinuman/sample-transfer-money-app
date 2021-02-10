package com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller

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
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_organization_transfer.*
import kotlinx.android.synthetic.main.fragment_frequent_billers.*
import kotlinx.android.synthetic.main.fragment_frequent_billers.textViewState
import kotlinx.android.synthetic.main.fragment_frequent_billers.viewLoadingState
import kotlinx.android.synthetic.main.widget_search_layout.*
import java.util.concurrent.TimeUnit

class FrequentBillerFragment :
    BaseFragment<FrequentBillerViewModel>(R.layout.fragment_frequent_billers),
    EpoxyAdapterCallback<FrequentBiller> {

    private val controller by lazyFast { FrequentBillerController(applicationContext, viewUtil) }

    private val pageable by lazyFast { Pageable() }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[FrequentBillerViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowFrequentBillerLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutFrequentBillers,
                        recyclerViewFrequentBillers,
                        textViewState
                    )
                    if (viewLoadingState.visibility == View.VISIBLE) {
                        updateController(mutableListOf())
                    }
                }
                is ShowFrequentBillerDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutFrequentBillers,
                        recyclerViewFrequentBillers
                    )
                }
                is ShowFrequentBillerEndlessLoading -> {
                    pageable.isLoadingPagination = true
                    updateController()
                }
                is ShowFrequentBillerEndlessDismissLoading -> {
                    pageable.isLoadingPagination = false
                    updateController()
                }
                is ShowFrequentBillerError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.frequentBillers.observe(this, Observer {
            it?.let {
                updateController(it)
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        if ((activity as BillerMainActivity)
                .intent.getStringExtra(BillerMainActivity.EXTRA_PERMISSION) != null
        ) {
            val permissionCollection = JsonHelper.fromJson<PermissionCollection>(
                (activity as BillerMainActivity).intent.getStringExtra(BillerMainActivity.EXTRA_PERMISSION)
            )
            if (permissionCollection.hasAllowToCreateBillsPaymentFrequent) {
                getFrequentBillers(true)
            } else {
                (activity as BillerMainActivity).setCurrentViewPager(1)
                (activity as BillerMainActivity).isInitialLoad = false
                swipeRefreshLayoutFrequentBillers.isEnabled = false
                textViewState?.visibility = View.VISIBLE
                textViewState?.text = getString(R.string.title_no_feature_permission)
                editTextSearch?.visibility = View.GONE
                editTextSearch?.isEnabled = false
            }
        }
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
        swipeRefreshLayoutFrequentBillers.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getFrequentBillers(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateController(data: MutableList<FrequentBiller> = getFrequentBillers()) {
        controller.setData(data, pageable)
    }

    override fun onClickItem(view: View, data: FrequentBiller, position: Int) {
        eventBus.inputSyncEvent.emmit(
            BaseEvent(InputSyncEvent.ACTION_INPUT_FREQUENT_BILLER, JsonHelper.toJson(data))
        )
        activity?.onBackPressed()
    }

    override fun onTapToRetry() {
        getFrequentBillers(isInitialLoading = false, isTapToRetry = true)
    }

    private fun getFrequentBillers(): MutableList<FrequentBiller> {
        return viewModel.frequentBillers.value.notNullable()
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(getAppCompatActivity())
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
                    pageable.filter = filter.text().toString().nullable()
                    getFrequentBillers(true)
                }
            }.addTo(disposables)
    }

    private fun showEmptyState(data: MutableList<FrequentBiller>) {
        textViewState.text = getString(R.string.title_no_frequent_biller)
        if (data.isNotEmpty()) {
            if ((activity as BillerMainActivity).isInitialLoad) {
                (activity as BillerMainActivity).setCurrentViewPager(0)
                (activity as BillerMainActivity).isInitialLoad = false
            }
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            if ((activity as BillerMainActivity).isInitialLoad) {
                (activity as BillerMainActivity).setCurrentViewPager(1)
                (activity as BillerMainActivity).isInitialLoad = false
            }
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        controller.setEpoxyAdapterCallback(this)
        val linearLayoutManager = getLinearLayoutManager()
        recyclerViewFrequentBillers.layoutManager = linearLayoutManager
        recyclerViewFrequentBillers.addOnScrollListener(
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
                    if (!pageable.isLoadingPagination) getFrequentBillers(false)
                }
            }
        )
        recyclerViewFrequentBillers.setController(controller)
    }

    private fun scrollToTop() {
        recyclerViewFrequentBillers.post {
            recyclerViewFrequentBillers.smoothScrollToPosition(0)
        }
    }

    private fun getFrequentBillers(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        val accountId = activity?.intent?.getStringExtra(BillerMainActivity.EXTRA_ACCOUNT_ID)
        if (accountId != null) {
            if (isTapToRetry) {
                pageable.refreshErrorPagination()
            }
            if (isInitialLoading) {
                pageable.resetPagination()
            } else {
                pageable.resetLoad()
            }
            viewModel.getFrequentBillers(
                accountId,
                pageable = pageable,
                isInitialLoading = isInitialLoading
            )
        }
    }

    companion object {
        fun newInstance(): FrequentBillerFragment {
            val fragment =
                FrequentBillerFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
