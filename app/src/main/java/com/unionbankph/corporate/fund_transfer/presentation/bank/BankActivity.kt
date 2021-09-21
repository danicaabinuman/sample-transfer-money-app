package com.unionbankph.corporate.fund_transfer.presentation.bank

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
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityBankBinding
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class BankActivity :
    BaseActivity<ActivityBankBinding, BankViewModel>(),
    BankController.AdapterCallbacks {

    private lateinit var controller: BankController

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(
            binding.viewToolbar.tvToolbar,
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
        binding.recyclerViewBank.setController(controller)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (!binding.swipeRefreshLayoutBanks.isRefreshing) {
                        binding.viewLoadingState.root.visibility = View.VISIBLE
                    }
                    if (binding.viewLoadingState.root.visibility == View.VISIBLE) {
                        binding.swipeRefreshLayoutBanks.isEnabled = true
                    }
                }
                is UiState.Complete -> {
                    if (!binding.swipeRefreshLayoutBanks.isRefreshing) {
                        binding.viewLoadingState.root.visibility = View.GONE
                    } else binding.swipeRefreshLayoutBanks.isRefreshing = false
                    binding.swipeRefreshLayoutBanks.isEnabled = true
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
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        binding.swipeRefreshLayoutBanks.apply {
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
        if (viewUtil.isSoftKeyboardShown(binding.constraintLayout)) viewUtil.dismissKeyboard(this)
        onBackPressed()
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.filterList(binding.viewSearchLayout.editTextSearch.text.toString())
                binding.viewSearchLayout.editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this@BankActivity)
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
                    viewModel.filterList(filter.text().toString())
                }
            }
            .addTo(disposables)
    }

    private fun showEmptyState(data: MutableList<SectionedData<Bank>>) {
        binding.textViewState.text = getString(R.string.title_no_banks)
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val CHANNEL_CHECK_DEPOSIT = "5"
    }

    override val viewModelClassType: Class<BankViewModel>
        get() = BankViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBankBinding
        get() = ActivityBankBinding::inflate
}
