package com.unionbankph.corporate.bills_payment.presentation.frequent_biller_list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.FrequentBillerViewModel
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.ShowFrequentBillerDismissLoading
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.ShowFrequentBillerEndlessDismissLoading
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.ShowFrequentBillerEndlessLoading
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.ShowFrequentBillerError
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.ShowFrequentBillerGetTestDataList
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.ShowFrequentBillerLoading
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_detail.ManageFrequentBillerDetailActivity
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form.ManageFrequentBillerFormActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.ActivityManageFrequentBillerBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 12/03/2019
 */
class ManageFrequentBillerActivity :
    BaseActivity<ActivityManageFrequentBillerBinding, FrequentBillerViewModel>(),
    EpoxyAdapterCallback<FrequentBiller>, OnTutorialListener {

    private val pageable by lazyFast { Pageable() }

    private var snackBarProgressBar: Snackbar? = null

    private val controller by lazyFast {
        ManageFrequentBillerController(this, viewUtil, autoFormatUtil)
    }

    private val tutorialController by lazyFast {
        ManageFrequentBillerController(this, viewUtil, autoFormatUtil)
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initRxSearchEventListener()
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getFrequentBillers(true)
            }
        }
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.requestFocus()
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        binding.floatingActionButtonAddFrequentBiller.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                ManageFrequentBillerFormActivity.EXTRA_TYPE,
                ManageFrequentBillerFormActivity.TYPE_CREATE
            )
            navigator.navigate(
                this,
                ManageFrequentBillerFormActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        tutorialEngineUtil.setOnTutorialListener(this)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initGeneralViewModel()
        initViewModel()
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_manage_frequent_billers),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowFrequentBillerLoading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        binding.textViewState
                    )
                    if (binding.viewLoadingState.root.visibility == View.VISIBLE) {
                        updateController(mutableListOf())
                    }
                }
                is ShowFrequentBillerDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowFrequentBillerEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowFrequentBillerEndlessDismissLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowFrequentBillerGetTestDataList -> {
                    startViewTutorial(it.data)
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
        getFrequentBillers(true)
    }

    private fun updateController(data: MutableList<FrequentBiller> = getFrequentBillers()) {
        controller.setData(data, pageable, isTableView())
    }

    private fun updateTutorialController(data: MutableList<FrequentBiller>) {
        tutorialController.setData(data, pageable, isTableView())
    }

    private fun getFrequentBillers(): MutableList<FrequentBiller> {
        return viewModel.frequentBillers.value.notNullable()
    }

    override fun onClickItem(view: View, data: FrequentBiller, position: Int) {
        val bundle = Bundle().apply {
            putString(
                ManageFrequentBillerDetailActivity.EXTRA_ID,
                data.id.toString()
            )
        }
        navigator.navigate(
            this,
            ManageFrequentBillerDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    override fun onTapToRetry() {
        getFrequentBillers(isInitialLoading = false, isTapToRetry = true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val helpMenu = menu.findItem(R.id.menu_help)
        helpMenu.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                viewModel.getFrequentBillersTestData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onClickSkipButtonTutorial(spotlight: Spotlight) {
        isSkipTutorial = true
        tutorialViewModel.skipTutorial()
        spotlight.closeSpotlight()
    }

    override fun onClickOkButtonTutorial(spotlight: Spotlight) {
        spotlight.closeCurrentTarget()
    }

    override fun onStartedTutorial(view: View?, viewTarget: View) {
        // onStartedTutorial
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            clearTutorial()
        } else {
            if (view != null) {
                when (view) {
                    getFirstItemTutorial() -> {
                        tutorialEngineUtil.startTutorial(
                            this,
                            binding.floatingActionButtonAddFrequentBiller,
                            R.layout.frame_tutorial_lower_right,
                            binding.floatingActionButtonAddFrequentBiller.height.toFloat() -
                                    resources.getDimension(R.dimen.grid_5_half),
                            true,
                            getString(R.string.msg_tutorial_frequent_biller_fab),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    binding.floatingActionButtonAddFrequentBiller -> {
                        tutorialEngineUtil.startTutorial(
                            this,
                            binding.viewSearchLayout.editTextSearch,
                            R.layout.frame_tutorial_upper_left,
                            resources.getDimension(R.dimen.field_radius_search),
                            false,
                            getString(R.string.msg_tutorial_frequent_biller_filter),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    binding.viewSearchLayout.editTextSearch -> {
                        clearTutorial()
                    }
                }
            } else {
                tutorialEngineUtil.startTutorial(
                    this,
                    getFirstItemTutorial(),
                    R.layout.frame_tutorial_upper_left,
                    if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
                    false,
                    getString(R.string.msg_tutorial_frequent_biller_item),
                    GravityEnum.BOTTOM,
                    OverlayAnimationEnum.ANIM_EXPLODE
                )
            }
        }
    }

    private fun clearTutorial() {
        updateTutorialController(mutableListOf())
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        getRecyclerViewTutorial().setController(tutorialController)
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = true
            if (isTableView()) {
                if (snackBarProgressBar == null) {
                    snackBarProgressBar = viewUtil.showCustomSnackBar(
                        binding.constraintLayout,
                        R.layout.widget_snackbar_progressbar,
                        Snackbar.LENGTH_INDEFINITE
                    )
                }
                snackBarProgressBar?.show()
            } else {
                updateController()
            }
        }
    }

    private fun dismissEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = false
            if (isTableView()) {
                snackBarProgressBar?.dismiss()
            } else {
                updateController()
            }
        }
    }

    private fun showEmptyState(data: MutableList<FrequentBiller>) {
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(data.isNotEmpty())
        }
        if (data.isNotEmpty()) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.text = getString(R.string.title_no_frequent_billers)
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_FREQUENT_BILLER_LIST ||
                it.eventType == ActionSyncEvent.ACTION_DELETE_FREQUENT_BILLER
            ) {
                if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
                    getSwipeRefreshLayout().isRefreshing = true
                }
                getFrequentBillers(true)
            }
        }.addTo(disposables)
    }

    private fun getFrequentBillers(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getFrequentBillers(
            pageable = pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun startViewTutorial(data: MutableList<FrequentBiller>) {
        getRecyclerView().visibility(false)
        updateTutorialController(data)
        viewUtil.animateRecyclerView(getRecyclerViewTutorial(), true)
        tutorialEngineUtil.startTutorial(
            this,
            R.drawable.ic_tutorial_beneficiaries,
            getString(R.string.title_manage_frequent_billers),
            getString(R.string.msg_tutorial_frequent_biller_landing)
        )
    }

    private fun getFirstItemTutorial(): View {
        return if (isTableView()) {
            getRecyclerViewTutorial()
                .findViewHolderForAdapterPosition(0)
                ?.itemView!!
        } else {
            getRecyclerViewTutorial()
                .findViewHolderForAdapterPosition(0)
                ?.itemView
                ?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewItem)!!
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    binding.viewSearchLayout.editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(this)
                    getFrequentBillers(true)
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
                    getFrequentBillers(true)
                }
            }.addTo(disposables)
    }

    private fun initRecyclerView() {
        initHeaderRow()
        controller.setAdapterCallbacks(this)
        val linearLayoutManager = getLinearLayoutManager()
        getRecyclerView().layoutManager = linearLayoutManager
        getRecyclerView().addOnScrollListener(
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
        getRecyclerView().setController(controller)

        getRecyclerViewTutorial().setController(tutorialController)
        tutorialController.setAdapterCallbacks(this)
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutFrequentBiller.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_manage_frequency)
                    .toMutableList()
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                textViewHeader.text = it
                binding.viewTable.linearLayoutRow.addView(viewRowHeader)
            }
        }
    }

    private fun getRecyclerView() =
        if (isTableView()) binding.viewTable.recyclerViewTable else binding.recyclerViewManageFrequentBiller

    private fun getRecyclerViewTutorial() =
        if (isTableView()) binding.viewTable.recyclerViewTableTutorial else binding.recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutFrequentBiller

    override val layoutId: Int
        get() = R.layout.activity_manage_frequent_biller

    override val viewModelClassType: Class<FrequentBillerViewModel>
        get() = FrequentBillerViewModel::class.java
}
