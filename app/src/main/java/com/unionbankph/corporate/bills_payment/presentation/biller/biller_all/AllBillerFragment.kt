package com.unionbankph.corporate.bills_payment.presentation.biller.biller_all

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
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_all_billers.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import java.util.concurrent.TimeUnit

class AllBillerFragment :
    BaseFragment<AllBillerViewModel>(R.layout.fragment_all_billers),
    EpoxyAdapterCallback<Biller>,
    OnConfirmationPageCallBack {

    private var billersHeaderResult: MutableList<Biller> = mutableListOf()

    private var billersResult: MutableList<Biller> = mutableListOf()

    private val controller by lazyFast { AllBillerController(applicationContext, viewUtil) }

    private val page by lazyFast { activity?.intent?.getStringExtra(BillerMainActivity.EXTRA_PAGE) }

    private var confirmationBottomSheet: ConfirmationBottomSheet? = null

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AllBillerViewModel::class.java]
        viewModel.allBillerState.observe(this, Observer {
            when (it) {
                is ShowAllBillerLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutAllBillers,
                        recyclerViewAllBillers,
                        textViewState
                    )
                }
                is ShowAllBillerDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutAllBillers,
                        recyclerViewAllBillers
                    )
                }
                is ShowAllBillerSuccess -> {
                    billersHeaderResult = it.listHeader
                    billersResult = it.list
                    updateController()
                    recyclerViewAllBillers?.visibility = View.VISIBLE
                    showEmptyState(billersResult)
                }
                is ShowAllBillerError -> {
                    (activity as BillerMainActivity).handleOnError(it.throwable)
                }
            }
        })
        getBillers()
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

        swipeRefreshLayoutAllBillers.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getBillers()
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

    private fun updateController() {
        controller.setData(billersHeaderResult, billersResult, page)
    }

    override fun onClickItem(view: View, data: Biller, position: Int) {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        if (!data.canAddAsFrequentBiller && page == BillerMainActivity.PAGE_FREQUENT_BILLER_FORM) {
            showTrustDeviceResultBottomSheet()
        } else {
            eventBus.inputSyncEvent.emmit(
                BaseEvent(
                    InputSyncEvent.ACTION_INPUT_BILLER,
                    JsonHelper.toJson(data)
                )
            )
            activity?.onBackPressed()
        }
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        confirmationBottomSheet?.dismiss()
    }

    private fun initRecyclerView() {
        controller.setEpoxyAdapterCallback(this)
        recyclerViewAllBillers.setController(controller)
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    filterList(editTextSearch.text.toString())
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
                if (filter.view().isFocused && billersResult.isNotEmpty()) {
                    filterList(filter.text().toString())
                }
            }.addTo(disposables)
    }

    private fun filterList(filter: String) {
        if (filter != "") {
            val searchResults = billersResult.asSequence()
                .filter { it.name!!.contains(filter, true) }
                .toMutableList()
            val searchResultsHeader = searchResults.asSequence()
                .distinctBy { Pair(it.name?.get(0), it.name?.get(0)) }
                .filter { it.name!!.contains(filter, true) }
                .toMutableList()
            controller.setData(searchResultsHeader, searchResults, page)
            showEmptyState(searchResults)
        } else {
            updateController()
            showEmptyState(billersResult)
        }
    }

    private fun showEmptyState(list: MutableList<Biller>) {
        textViewState.text = getString(R.string.title_no_biller)
        if (list.size > 0) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun getBillers() {
        val type = activity?.intent?.getStringExtra(BillerMainActivity.EXTRA_TYPE)
        val permission = activity?.intent?.getStringExtra(BillerMainActivity.EXTRA_PERMISSION)
        if (type != null && permission != null) {
            val permissionCollection = JsonHelper.fromJson<PermissionCollection>(permission)
            if (permissionCollection.hasAllowToCreateBillsPaymentAdhoc) {
                viewModel.getBillers(type)
            } else {
                textViewState.text = getString(R.string.title_no_feature_permission)
                textViewState.visibility = View.VISIBLE
                swipeRefreshLayoutAllBillers.isEnabled = false
            }
        } else {
            if (type != null) {
                viewModel.getBillers(type)
            }
        }
    }

    private fun showTrustDeviceResultBottomSheet() {
        confirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_biller_not_allowed),
            formatString(R.string.msg_biller_not_allowed),
            null,
            formatString(R.string.action_close)
        )
        confirmationBottomSheet?.setOnConfirmationPageCallBack(this)
        confirmationBottomSheet?.show(
            childFragmentManager,
            AllBillerFragment::class.java.simpleName
        )
    }

    companion object {
        fun newInstance(): AllBillerFragment {
            val fragment =
                AllBillerFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }

        const val TYPE_BILLER = "biller"
    }
}
