package com.unionbankph.corporate.fund_transfer.presentation.bank

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
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_bank.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.concurrent.TimeUnit

class BankActivity :
    BaseActivity<BankViewModel>(R.layout.activity_bank), BankController.AdapterCallbacks {

    private lateinit var controller: BankController

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(
            tvToolbar,
            formatString(
                if (intent.getStringExtra(EXTRA_CHANNEL_ID) == CHANNEL_CHECK_DEPOSIT) {
                    R.string.title_issuing_bank
                } else {
                    R.string.hint_select_receiving_bank
                }
            )
        )
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        controller = BankController(this)
        recyclerViewBank.setController(controller)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[BankViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (!swipeRefreshLayoutBanks.isRefreshing) {
                        viewLoadingState?.visibility = View.VISIBLE
                    }
                    if (viewLoadingState?.visibility == View.VISIBLE) {
                        swipeRefreshLayoutBanks.isEnabled = true
                    }
                }
                is UiState.Complete -> {
                    if (!swipeRefreshLayoutBanks.isRefreshing) {
                        viewLoadingState?.visibility = View.GONE
                    } else swipeRefreshLayoutBanks.isRefreshing = false
                    swipeRefreshLayoutBanks.isEnabled = true
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.banks.observe(this, Observer {
            it?.let {
                updateController(it)
                showEmptyState(it)
            }
        })
        fetchBanks()
    }

    private fun fetchBanks() {
        viewModel.getBanks(intent.getStringExtra(EXTRA_CHANNEL_ID))
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
        swipeRefreshLayoutBanks.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchBanks()
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

    private fun updateController(data: MutableList<SectionedData<Bank>>) {
        controller.setData(data)
    }

    override fun onClickItem(model: String?) {
        eventBus.inputSyncEvent.emmit(BaseEvent(InputSyncEvent.ACTION_INPUT_BANK_RECEIVER, model!!))
        if (viewUtil.isSoftKeyboardShown(constraintLayout)) viewUtil.dismissKeyboard(this)
        onBackPressed()
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.filterList(editTextSearch.text.toString())
                editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this@BankActivity)
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
                    viewModel.filterList(filter.text().toString())
                }
            }
            .addTo(disposables)
    }

    private fun showEmptyState(data: MutableList<SectionedData<Bank>>) {
        textViewState.text = getString(R.string.title_no_banks)
        if (data.size > 0) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val CHANNEL_CHECK_DEPOSIT = "5"
    }
}
