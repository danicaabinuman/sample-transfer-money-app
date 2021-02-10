package com.unionbankph.corporate.branch.presentation.branch

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_branch.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.concurrent.TimeUnit

class BranchActivity :
    BaseActivity<BranchViewModel>(R.layout.activity_branch),
    EpoxyAdapterCallback<Branch> {

    private val controller by lazyFast { BranchController(this, viewUtil) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, formatString(R.string.title_select_branch))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initRxSearchEventListener()
        swipeRefreshLayoutBranchTransaction.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getBranches()
            }
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
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

    override fun onClickItem(view: View, data: Branch, position: Int) {
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                InputSyncEvent.ACTION_INPUT_BRANCH,
                JsonHelper.toJson(data)
            )
        )
        onBackPressed()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[BranchViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowBranchLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutBranchTransaction,
                        recyclerViewBranchTransaction,
                        textViewState
                    )
                }
                is ShowBranchDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutBranchTransaction,
                        recyclerViewBranchTransaction
                    )
                }
                is ShowBranchError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        getBranches()
        viewModel.branchesState.observe(this, Observer {
            showEmptyState(it)
            updateController(it)
        })
    }

    private fun updateController(data: MutableList<Branch>) {
        controller.setData(data)
    }

    private fun showEmptyState(data: MutableList<Branch>) {
        if (data.size > 0) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        recyclerViewBranchTransaction.setController(controller)
    }

    private fun initRxSearchEventListener() {
        imageViewClearText.setOnClickListener {
            editTextSearch.requestFocus()
            editTextSearch.text?.clear()
        }
        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val filteredList = viewModel.filterList(editTextSearch.text.toString())
                updateController(filteredList)
                showEmptyState(filteredList)
                editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this)
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
                if (filter.view().isFocused && getBranchesLiveData().isNotEmpty()) {
                    val filteredList = viewModel.filterList(filter.text().toString())
                    updateController(filteredList)
                    showEmptyState(filteredList)
                }
            }
            .addTo(disposables)
    }

    private fun getBranchesLiveData(): MutableList<Branch> {
        return viewModel.branchesState.value.notNullable()
    }

    private fun getBranches() {
        viewModel.getBranches()
    }
}
