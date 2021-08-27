package com.unionbankph.corporate.branch.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.branch.presentation.channel.BranchVisitChannelActivity
import com.unionbankph.corporate.branch.presentation.detail.BranchVisitDetailActivity
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.ActivityBranchVisitBinding
import io.reactivex.rxkotlin.addTo

/**
 * Created by herald25santos on 2019-11-05
 */
class BranchVisitActivity :
    BaseActivity<ActivityBranchVisitBinding, BranchVisitViewModel>(),
    EpoxyAdapterCallback<BranchVisit>, OnTutorialListener {

    private val pageable by lazyFast { Pageable() }

    private val controller by lazyFast { BranchVisitController(this, viewUtil) }

    private val tutorialController by lazyFast { BranchVisitController(this, viewUtil) }

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
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        binding.fabCheckDeposit.setOnClickListener {
            navigator.navigate(
                this,
                BranchVisitChannelActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchBranchVisits(true)
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
                viewModel.getBranchVisitsTestData()
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

    override fun onClickItem(view: View, data: BranchVisit, position: Int) {
        navigateBranchVisitDetailScreen(data)
    }

    override fun onTapToRetry() {
        fetchBranchVisits(isInitialLoading = false, isTapToRetry = true)
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

    private fun initCheckDepositViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowBranchVisitLoading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        binding.textViewState
                    )
                }
                is ShowBranchVisitDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowBranchVisitEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowBranchVisitDismissEndlessLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowBranchVisitError -> {
                    handleOnError(it.throwable)
                }
            }
        })

        viewModel.testBranchVisitsLiveData.observe(this, Observer {
            startViewTutorial(false)
        })
        viewModel.branchVisitsLiveData.observe(this, Observer {
            it?.let {
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
                updateController(it)
            }
        })
        fetchBranchVisits(true)
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
                        formatString(R.string.title_branch_transaction),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_BRANCH_VISIT_LIST) {
                if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
                    getSwipeRefreshLayout().isRefreshing = true
                }
                fetchBranchVisits(true)
            }
        }.addTo(disposables)
    }

    private fun fetchBranchVisits(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getBranchVisits(
            pageable = pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun updateController(data: MutableList<BranchVisit>) {
        controller.setData(data, pageable, isTableView())
    }

    private fun updateTutorialController() {
        tutorialController.setData(
            getTestBranchVisitsLiveData(),
            pageable,
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
        viewModel.testBranchVisitsLiveData.value?.clear()
        updateTutorialController()
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        getRecyclerViewTutorial().setController(tutorialController)
        if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
            showEmptyState(viewModel.testBranchVisitsLiveData.value.notNullable())
        }
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
                updateController(getBranchVisitsLiveData())
            }
        }
    }

    private fun dismissEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = false
            if (isTableView()) {
                snackBarProgressBar?.dismiss()
            } else {
                updateController(getBranchVisitsLiveData())
            }
        }
    }

    private fun getBranchVisitsLiveData(): MutableList<BranchVisit> {
        return viewModel.branchVisitsLiveData.value.notNullable()
    }

    private fun getTestBranchVisitsLiveData(): MutableList<BranchVisit> {
        return viewModel.testBranchVisitsLiveData.value.notNullable()
    }

    private fun showEmptyState(data: MutableList<BranchVisit>) {
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

    private fun initRecyclerView() {
        initHeaderRow()
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
                    if (!pageable.isLoadingPagination) fetchBranchVisits(false)
                }
            }
        )
        getRecyclerView().setController(controller)
        controller.setAdapterCallbacks(this)

        getRecyclerViewTutorial().setController(tutorialController)
        tutorialController.setAdapterCallbacks(this)
    }

    private fun navigateBranchVisitDetailScreen(data: BranchVisit) {
        val bundle = Bundle().apply {
            putString(
                BranchVisitDetailActivity.EXTRA_ID,
                data.id
            )
        }
        navigator.navigate(
            this,
            BranchVisitDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutBranchVisit.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_branch_visit)
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
        if (isTableView()) binding.viewTable.recyclerViewTable else binding.recyclerViewBranchVisit

    private fun getRecyclerViewTutorial() =
        if (isTableView()) binding.viewTable.recyclerViewTableTutorial else binding.recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutBranchVisit

    override val viewModelClassType: Class<BranchVisitViewModel>
        get() = BranchVisitViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBranchVisitBinding
        get() = ActivityBranchVisitBinding::inflate
}
