package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection

import android.os.Bundle
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
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentBeneficiaryBinding
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 12/03/2019
 */
class BeneficiaryFragment :
    BaseFragment<FragmentBeneficiaryBinding, BeneficiaryViewModel>(),
    EpoxyAdapterCallback<Beneficiary> {

    private val controller by lazyFast { BeneficiaryController(applicationContext, viewUtil) }

    private val pageable by lazyFast { Pageable() }

    private var beneficiaryMasters: MutableList<Beneficiary> = mutableListOf()

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowBeneficiaryLoading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        binding.swipeRefreshLayoutBeneficiary,
                        binding.recyclerViewBeneficiary,
                        binding.textViewState
                    )
                }
                is ShowBeneficiaryDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        binding.swipeRefreshLayoutBeneficiary,
                        binding.recyclerViewBeneficiary,
                    )
                }
                is ShowBeneficiaryEndlessLoading -> {
                    pageable.isLoadingPagination = true
                    updateController()
                }
                is ShowBeneficiaryDismissEndlessLoading -> {
                    pageable.isLoadingPagination = false
                    updateController()
                }
                is ShowBeneficiaryError -> {
                    (activity as BeneficiaryActivity).handleOnError(it.throwable)
                }
            }
        })
        viewModel.beneficiaryMasterLiveData.observe(this, Observer {
            it?.let { pagedDto ->
                pageable.apply {
                    totalPageCount = pagedDto.totalPages
                    isLastPage = !pagedDto.hasNextPage
                    increasePage()
                }
                if (pagedDto.currentPage == 0) {
                    beneficiaryMasters = pagedDto.results
                    showEmptyState()
                } else {
                    beneficiaryMasters.addAll(pagedDto.results)
                }
                updateController()
            }
        })
        fetchBeneficiaries(true)
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
                binding.viewSearchLayout.editTextSearch.requestFocus()
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        binding.swipeRefreshLayoutBeneficiary.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchBeneficiaries(true)
            }
        }
    }

    override fun onClickItem(view: View, data: Beneficiary, position: Int) {
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                InputSyncEvent.ACTION_INPUT_BENEFICIARY,
                JsonHelper.toJson(data)
            )
        )
    }

    override fun onTapToRetry() {
        fetchBeneficiaries(isInitialLoading = false, isTapToRetry = true)
    }

    private fun updateController() {
        controller.setData(beneficiaryMasters, pageable)
    }

    private fun initRecyclerView() {
        controller.setEpoxyAdapterCallback(this)
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewBeneficiary.layoutManager = linearLayoutManager
        binding.recyclerViewBeneficiary.addOnScrollListener(
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
                    if (!pageable.isLoadingPagination) fetchBeneficiaries(false)
                }
            }
        )
        binding.recyclerViewBeneficiary.setController(controller)
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    binding.viewSearchLayout.editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(getAppCompatActivity())
                    fetchBeneficiaries(true)
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
                    pageable.filter = filter.text().toString().nullable()
                    fetchBeneficiaries(true)
                }
            }.addTo(disposables)
    }

    private fun showEmptyState() {
        if (beneficiaryMasters.size > 0) {
            if ((activity as BeneficiaryActivity).isInitialLoad) {
                (activity as BeneficiaryActivity).isInitialLoad = false
                (activity as BeneficiaryActivity).setCurrentViewPager(0)
            }
            if (binding.textViewState.visibility == View.VISIBLE)
                binding.textViewState.visibility = View.GONE
        } else {
            if ((activity as BeneficiaryActivity).isInitialLoad) {
                (activity as BeneficiaryActivity).isInitialLoad = false
                (activity as BeneficiaryActivity).setCurrentViewPager(1)
            }
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun fetchBeneficiaries(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (activity?.intent?.getStringExtra(BeneficiaryActivity.EXTRA_PERMISSION) != null) {
            val permissionCollection = JsonHelper.fromJson<PermissionCollection>(
                activity?.intent?.getStringExtra(BeneficiaryActivity.EXTRA_PERMISSION)
            )
            if (permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster) {
                if (isTapToRetry) {
                    pageable.refreshErrorPagination()
                }
                if (isInitialLoading) {
                    pageable.resetPagination()
                } else {
                    pageable.resetLoad()
                }
                viewModel.getBeneficiaries(
                    channelId = arguments?.getString(EXTRA_ID).notNullable(),
                    sourceId = arguments?.getString(EXTRA_SOURCE_ID).notNullable(),
                    pageable = pageable,
                    isInitialLoading = isInitialLoading
                )
            } else {
                binding.textViewState.text = getString(R.string.title_no_feature_permission)
                binding.textViewState.visibility = View.VISIBLE
                binding.swipeRefreshLayoutBeneficiary.isEnabled = false
            }
        }
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_SOURCE_ID = "source_id"

        fun newInstance(id: String, sourceId: String): BeneficiaryFragment {
            val fragment = BeneficiaryFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_ID, id)
            bundle.putString(EXTRA_SOURCE_ID, sourceId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_beneficiary

    override val viewModelClassType: Class<BeneficiaryViewModel>
        get() = BeneficiaryViewModel::class.java
}
