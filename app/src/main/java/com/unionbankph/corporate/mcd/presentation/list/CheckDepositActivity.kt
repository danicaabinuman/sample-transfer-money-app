package com.unionbankph.corporate.mcd.presentation.list

import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.presentation.detail.CheckDepositDetailActivity
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingActivity
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.ActivityCheckDepositBinding
import com.unionbankph.corporate.mcd.presentation.filter.CheckDepositFilterActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 2019-11-05
 */
class CheckDepositActivity :
    BaseActivity<ActivityCheckDepositBinding, CheckDepositViewModel>(),
    EpoxyAdapterCallback<CheckDeposit>, OnTutorialListener {

    private val controller by lazyFast { CheckDepositController(this, viewUtil, autoFormatUtil) }

    private val tutorialController by lazyFast {
        CheckDepositController(this, viewUtil, autoFormatUtil)
    }

    private var snackBarProgressBar: Snackbar? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initGeneralViewModel()
        initCheckDepositViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupOutputs()
        setupViews()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initRxSearchEventListener()
        initClickListener()
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getCheckDeposits(true)
            }
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                viewModel.getCheckDepositsTestData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val filterMenu = menu.findItem(R.id.menu_filter)
        val helpMenu = menu.findItem(R.id.menu_help)
        filterMenu.isVisible = false
        helpMenu.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onClickItem(view: View, data: CheckDeposit, position: Int) {
        val bundle = Bundle()
        bundle.putString(
            CheckDepositDetailActivity.EXTRA_ID,
            data.id
        )
        navigator.navigate(
            this,
            CheckDepositDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    override fun onTapToRetry() {
        getCheckDeposits(isInitialLoading = false, isTapToRetry = true)
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
                            binding.fabCheckDeposit,
                            R.layout.frame_tutorial_lower_right,
                            binding.fabCheckDeposit.height.toFloat() -
                                    resources.getDimension(R.dimen.grid_5_half),
                            true,
                            getString(R.string.msg_tutorial_beneficiary_fab),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    binding.fabCheckDeposit -> {
                        clearTutorial()
                    }
                }
            } else {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    tutorialEngineUtil.startTutorial(
                        this,
                        getFirstItemTutorial(),
                        R.layout.frame_tutorial_upper_left,
                        if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
                        false,
                        getString(R.string.msg_tutorial_bills_payment_sample),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                } else {
                    clearTutorial()
                    tutorialViewModel.setTutorial(TutorialScreenEnum.BILLS_PAYMENT, false)
                }
            }
        }
    }

    private fun setupViews() {
        binding.viewSearchLayout.imageViewFilter.isVisible = true
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
    }

    private fun setupOutputs() {
        viewModel.checkDepositFilterCount.subscribe {
            it?.let {
                val hasBadge = it > 0
                binding.viewSearchLayout.viewBadgeCount.root.visibility(hasBadge)
                binding.viewSearchLayout.viewBadgeCount.textViewBadgeCount.text = it.toString()
                binding.viewSearchLayout.viewBadgeCount.textViewBadgeCount.setContextCompatBackground(R.drawable.circle_solid_orange)
                if (hasBadge) {
                    binding.viewSearchLayout.imageViewFilter.setImageResource(R.drawable.ic_filter_orange)
                } else {
                    binding.viewSearchLayout.imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                }
            }
            if (it == 0L) {
                binding.viewSearchLayout.imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                binding.viewSearchLayout.viewBadgeCount.root.visibility(false)
            }
        }.addTo(disposables)
        viewModel.checkDepositFilterCount.subscribe {
            getCheckDeposits(true)
        }.addTo(disposables)
    }

    private fun initCheckDepositViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowCheckDepositLoading -> {
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
                is ShowCheckDepositDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowCheckDepositEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowCheckDepositDismissEndlessLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowCheckDepositError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.checkDeposits.observe(this, Observer {
            it?.let {
                if (viewModel.pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
                updateController(it)
            }
        })
        viewModel.testCheckDeposits.observe(this, Observer {
            //            it?.let {
//                startViewTutorial(false)
//            }
        })
        viewModel.clickFilter.observe(this, EventObserver {
            navigateCheckDepositFilter(it)
        })
        getCheckDeposits(true)
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialHasTutorial -> {
                    if (it.hasTutorial) {
                        startViewTutorial(true)
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.CHECK_DEPOSIT)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_check_deposit),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_CHECK_DEPOSIT_LIST) {
                if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
                    getSwipeRefreshLayout().isRefreshing = true
                }
                getCheckDeposits(true)
            } else if (it.eventType == ActionSyncEvent.ACTION_APPLY_FILTER_CHECK_DEPOSIT) {
                viewModel.onApplyFilter(it.payload)
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        binding.fabCheckDeposit.setOnClickListener {
            navigator.navigate(
                this,
                CheckDepositOnBoardingActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        binding.viewSearchLayout.imageViewFilter.setOnClickListener {
            viewModel.onClickedFilter()
        }
        binding.viewSearchLayout.imageViewClearText.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            binding.viewSearchLayout.editTextSearch.text?.clear()
        }
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.viewSearchLayout.editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this)
                getCheckDeposits(true)
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
                    viewModel.pageable.filter = filter.text().toString().nullable()
                    getCheckDeposits(true)
                }
            }
            .addTo(disposables)
    }

    private fun getCheckDeposits(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getCheckDeposits(isInitialLoading)
    }

    private fun updateController(data: MutableList<CheckDeposit> = getCheckDepositsLiveData()) {
        controller.setData(data, viewModel.pageable, isTableView())
    }

    private fun updateTutorialController() {
        tutorialController.setData(
            getTestCheckDepositsLiveData(),
            viewModel.pageable,
            isTableView()
        )
    }

    private fun startViewTutorial(isInitial: Boolean) {
        if (!isInitial) {
            getRecyclerView().visibility(false)
            if (isTableView()) {
                binding.viewTable.linearLayoutRow.visibility(true)
            }
            updateTutorialController()
            viewUtil.animateRecyclerView(getRecyclerViewTutorial(), true)
        }
        tutorialEngineUtil.startTutorial(
            this,
            R.drawable.ic_tutorial_bills_payment_orange,
            getString(R.string.title_bills_payment),
            getString(R.string.msg_tutorial_bills_payment)
        )
    }

    private fun clearTutorial() {
        getTestCheckDepositsLiveData().clear()
        updateTutorialController()
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        getRecyclerViewTutorial().setController(tutorialController)
        if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
            showEmptyState(getCheckDepositsLiveData())
        }
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
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
            if (isTableView()) {
                snackBarProgressBar?.dismiss()
            } else {
                updateController()
            }
        }
    }

    private fun showEmptyState(data: MutableList<CheckDeposit>) {
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(data.size > 0)
        }
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().smoothScrollToPosition(0)
        }
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
                ?.findViewById<CardView>(R.id.cardViewContent)!!
        }
    }

    private fun getCheckDepositsLiveData(): MutableList<CheckDeposit> {
        return viewModel.checkDeposits.value.notNullable()
    }

    private fun getTestCheckDepositsLiveData(): MutableList<CheckDeposit> {
        return viewModel.testCheckDeposits.value.notNullable()
    }

    private fun initRecyclerView() {
        initHeaderRow()
        val linearLayoutManager = getLinearLayoutManager()
        getRecyclerView().layoutManager = linearLayoutManager
        getRecyclerView().addOnScrollListener(
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
                    if (!viewModel.pageable.isLoadingPagination) getCheckDeposits(false)
                }
            }
        )
        getRecyclerView().setController(controller)
        controller.setAdapterCallbacks(this)

        getRecyclerViewTutorial().setController(tutorialController)
        tutorialController.setAdapterCallbacks(this)
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutCheckDeposit.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_organization_payments)
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

    private fun navigateCheckDepositFilter(it: String?) {
        val bundle = Bundle().apply {
            if (it != "") {
                putString(CheckDepositFilterActivity.EXTRA_CURRENT_FILTER, it)
            }
        }
        navigator.navigate(
            this,
            CheckDepositFilterActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun getRecyclerView() =
        if (isTableView()) binding.viewTable.recyclerViewTable else binding.recyclerViewCheckDeposit

    private fun getRecyclerViewTutorial() =
        if (isTableView()) binding.viewTable.recyclerViewTableTutorial else binding.recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutCheckDeposit

    override val layoutId: Int
        get() = R.layout.activity_check_deposit

    override val viewModelClassType: Class<CheckDepositViewModel>
        get() = CheckDepositViewModel::class.java
}
