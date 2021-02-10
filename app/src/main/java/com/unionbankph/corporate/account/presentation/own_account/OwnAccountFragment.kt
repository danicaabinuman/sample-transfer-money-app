package com.unionbankph.corporate.account.presentation.own_account

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionController
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_own_account.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 12/03/2019
 */
class OwnAccountFragment :
    BaseFragment<OwnAccountViewModel>(R.layout.fragment_own_account),
    AccountAdapterCallback {

    private val controller by lazyFast {
        AccountSelectionController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    private val permissionId by lazyFast { arguments?.getString(EXTRA_PERMISSION_ID) }

    private val sourceAccountId by lazyFast { arguments?.getString(EXTRA_SOURCE_ID) }

    private val destinationId by lazyFast { arguments?.getString(EXTRA_DESTINATION_ID) }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initRxSearchEventListener()
        dataBus.accountDataBus.flowable.subscribe {
            viewModel.updateEachAccount(it)
        }.addTo(disposables)
        swipeRefreshLayoutAccounts.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccountsPermission(true)
            }
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[OwnAccountViewModel::class.java]
        viewModel.stateOwn.observe(this, Observer {
            when (it) {
                is ShowOwnAccountLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        showLoading(
                            viewLoadingState,
                            swipeRefreshLayoutAccounts,
                            recyclerViewAccounts,
                            textViewState
                        )
                        if (viewLoadingState.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    }
                }
                is ShowOwnAccountDismissLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        updateController()
                    } else {
                        dismissLoading(
                            viewLoadingState,
                            swipeRefreshLayoutAccounts,
                            recyclerViewAccounts
                        )
                    }
                }
                is ShowAccountLoadingOwnAccountDetail -> {
                    updateController()
                }
                is ShowAccountDismissLoadingOwnAccountDetail -> {
                    updateController()
                }
                is ShowOwnAccountDetailError,
                is ShowOwnAccountsSelectionDetailError -> {
                    updateController()
                }
                is ShowOwnAccountError -> {
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
        getAccountsPermission(true)
    }

    override fun onClickItem(account: String, position: Int) {
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                InputSyncEvent.ACTION_INPUT_OWN_ACCOUNT,
                account
            )
        )
    }

    override fun onTapErrorRetry(id: String, position: Int) {
        viewModel.getCorporateUserAccountDetail(id, position)
    }

    override fun onTapToRetry() {
        getAccountsPermission(isInitialLoading = false, isTapToRetry = true)
    }

    private fun updateController(data: MutableList<Account> = viewModel.getAccounts()) {
        controller.setData(data, viewModel.pageable)
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        recyclerViewAccounts.layoutManager = linearLayoutManager
        recyclerViewAccounts.addOnScrollListener(
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
                    if (!viewModel.pageable.isLoadingPagination)
                        getAccountsPermission(false)
                }
            }
        )
        controller.setAdapterCallbacks(this)
        recyclerViewAccounts.layoutManager = linearLayoutManager
        recyclerViewAccounts.setController(controller)
    }

    private fun initRxSearchEventListener() {
        RxView.clicks(imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                editTextSearch.text?.clear()
            }.addTo(disposables)
        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(getAppCompatActivity())
                getAccountsPermission(true)
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
                    viewModel.pageable.filter = filter.text().toString().nullable()
                    getAccountsPermission(true)
                }
            }
            .addTo(disposables)
    }

    private fun showEmptyState(data: MutableList<Account> = viewModel.getAccounts()) {
        if (data.size > 0) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        recyclerViewAccounts.post {
            recyclerViewAccounts.scrollToPosition(0)
        }
    }

    private fun getAccountsPermission(
        isInitialLoading: Boolean,
        isTapToRetry: Boolean = false
    ) {
        if (activity?.intent?.getStringExtra(BeneficiaryActivity.EXTRA_PERMISSION) != null) {
            val permissionCollection = JsonHelper.fromJson<PermissionCollection>(
                activity?.intent?.getStringExtra(BeneficiaryActivity.EXTRA_PERMISSION)
            )
            if (permissionCollection.hasAllowToCreateTransactionOwn) {
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
                    permissionId = permissionId.toString(),
                    sourceAccountId = sourceAccountId,
                    destinationId = destinationId
                )
            } else {
                textViewState.text = getString(R.string.title_no_feature_permission)
                textViewState.visibility = View.VISIBLE
                swipeRefreshLayoutAccounts.isEnabled = false
            }
        }
    }

    companion object {
        const val EXTRA_DESTINATION_ID = "destination_id"
        const val EXTRA_SOURCE_ID = "source_id"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_PERMISSION_ID = "permission_id"
        const val EXTRA_CURRENCY = "currency"

        fun newInstance(
            sourceId: String,
            destinationId: String?,
            channelId: String,
            permissionId: String,
            currency: String
        ): OwnAccountFragment {
            val fragment =
                OwnAccountFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_SOURCE_ID, sourceId)
            bundle.putString(EXTRA_DESTINATION_ID, destinationId)
            bundle.putString(EXTRA_CHANNEL_ID, channelId)
            bundle.putString(EXTRA_PERMISSION_ID, permissionId)
            bundle.putString(EXTRA_CURRENCY, currency)
            fragment.arguments = bundle
            return fragment
        }
    }
}
