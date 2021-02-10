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
import com.unionbankph.corporate.mcd.presentation.filter.CheckDepositFilterActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit.*
import kotlinx.android.synthetic.main.activity_check_deposit.constraintLayout
import kotlinx.android.synthetic.main.activity_check_deposit.recyclerViewTutorial
import kotlinx.android.synthetic.main.activity_check_deposit.swipeRefreshLayoutTable
import kotlinx.android.synthetic.main.activity_check_deposit.textViewState
import kotlinx.android.synthetic.main.activity_check_deposit.viewLoadingState
import kotlinx.android.synthetic.main.activity_check_deposit.viewToolbar
import kotlinx.android.synthetic.main.activity_organization_transfer.*
import kotlinx.android.synthetic.main.widget_badge_small.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_table_view.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 2019-11-05
 */
class CheckDepositActivity : BaseActivity<CheckDepositViewModel>(R.layout.activity_check_deposit),
                             EpoxyAdapterCallback<CheckDeposit>, OnTutorialListener {

    private val controller by lazyFast { CheckDepositController(this, viewUtil, autoFormatUtil) }

    private val tutorialController by lazyFast {
        CheckDepositController(this, viewUtil, autoFormatUtil)
    }

    private var snackBarProgressBar: Snackbar? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
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
                            fabCheckDeposit,
                            R.layout.frame_tutorial_lower_right,
                            fabCheckDeposit.height.toFloat() -
                                    resources.getDimension(R.dimen.grid_5_half),
                            true,
                            getString(R.string.msg_tutorial_beneficiary_fab),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    fabCheckDeposit -> {
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
        imageViewFilter.isVisible = true
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
    }

    private fun setupOutputs() {
        viewModel.checkDepositFilterCount.subscribe {
            it?.let {
                val hasBadge = it > 0
                viewBadgeCount.visibility(hasBadge)
                textViewBadgeCount.text = it.toString()
                textViewBadgeCount.setContextCompatBackground(R.drawable.circle_solid_orange)
                if (hasBadge) {
                    imageViewFilter.setImageResource(R.drawable.ic_filter_orange)
                } else {
                    imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                }
            }
            if (it == 0L) {
                imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                viewBadgeCount.visibility(false)
            }
        }.addTo(disposables)
        viewModel.checkDepositFilterCount.subscribe {
            getCheckDeposits(true)
        }.addTo(disposables)
    }

    private fun initCheckDepositViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[CheckDepositViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowCheckDepositLoading -> {
                    showLoading(
                        viewLoadingState,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        textViewState
                    )
                    if (viewLoadingState.visibility == View.VISIBLE) {
                        updateController(mutableListOf())
                    }
                }
                is ShowCheckDepositDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
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
                        textViewTitle,
                        textViewCorporationName,
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
                if (viewLoadingState.visibility != View.VISIBLE) {
                    getSwipeRefreshLayout().isRefreshing = true
                }
                getCheckDeposits(true)
            } else if (it.eventType == ActionSyncEvent.ACTION_APPLY_FILTER_CHECK_DEPOSIT) {
                viewModel.onApplyFilter(it.payload)
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        fabCheckDeposit.setOnClickListener {
            navigator.navigate(
                this,
                CheckDepositOnBoardingActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        imageViewFilter.setOnClickListener {
            viewModel.onClickedFilter()
        }
        imageViewClearText.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            editTextSearch.text?.clear()
        }
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this)
                getCheckDeposits(true)
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
                linearLayoutRow.visibility(true)
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
        if (viewLoadingState.visibility != View.VISIBLE) {
            showEmptyState(getCheckDepositsLiveData())
        }
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            if (isTableView()) {
                if (snackBarProgressBar == null) {
                    snackBarProgressBar = viewUtil.showCustomSnackBar(
                        constraintLayout,
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
            linearLayoutRow.visibility(data.size > 0)
        }
        if (data.size > 0) {
            if (textViewState?.visibility == View.VISIBLE) textViewState?.visibility = View.GONE
        } else {
            textViewState?.visibility = View.VISIBLE
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
            swipeRefreshLayoutTable.visibility(true)
            swipeRefreshLayoutCheckDeposit.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_organization_payments)
                    .toMutableList()
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                textViewHeader.text = it
                linearLayoutRow.addView(viewRowHeader)
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
        if (isTableView()) recyclerViewTable else recyclerViewCheckDeposit

    private fun getRecyclerViewTutorial() =
        if (isTableView()) recyclerViewTableTutorial else recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) swipeRefreshLayoutTable else swipeRefreshLayoutCheckDeposit
}
