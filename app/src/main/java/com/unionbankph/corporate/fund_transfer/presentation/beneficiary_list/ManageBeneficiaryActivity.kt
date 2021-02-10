package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
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
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.corporate.presentation.channel.ChannelActivity
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_detail.ManageBeneficiaryDetailActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_manage_beneficiary.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_table_view.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ManageBeneficiaryActivity :
    BaseActivity<ManageBeneficiaryViewModel>(R.layout.activity_manage_beneficiary),
    OnTutorialListener,
    EpoxyAdapterCallback<Beneficiary> {

    private val pageable by lazyFast { Pageable() }

    private var currentStatus: String = ""

    private var snackBarProgressBar: Snackbar? = null

    private val manageBeneficiaryController by lazyFast {
        ManageBeneficiaryController(applicationContext, viewUtil, autoFormatUtil)
    }

    private val tutorialManageBeneficiaryController by lazyFast {
        ManageBeneficiaryController(applicationContext, viewUtil, autoFormatUtil)
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
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
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_manage_beneficiary),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[ManageBeneficiaryViewModel::class.java]
        viewModel.beneficiaryState.observe(this, Observer {
            when (it) {
                is ShowManageBeneficiaryLoading -> {
                    showLoading(
                        viewLoadingState,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        textViewState,
                        linearLayoutRow
                    )
                    if (viewLoadingState.visibility == View.VISIBLE) {
                        updateController(mutableListOf())
                    }
                }
                is ShowManageBeneficiaryDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowManageBeneficiaryEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowManageBeneficiaryDismissEndlessLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowManageBeneficiaryGetTestDataList -> {
                    startViewTutorial(it.data)
                }
                is ShowManageBeneficiaryGetBeneficiaryCreationPermission -> {
                    floatingActionButtonAddBeneficiary.visibility(it.hasPermission)
                }
                is ShowManageBeneficiaryError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.beneficiaries.observe(this, Observer {
            it?.let {
                updateController(it)
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        fetchBeneficiaries(true)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
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
                    if (!pageable.isLoadingPagination) fetchBeneficiaries(false)
                }
            }
        )
        getRecyclerView().setController(manageBeneficiaryController)
        manageBeneficiaryController.setAdapterCallbacks(this)

        getRecyclerViewTutorial().setController(tutorialManageBeneficiaryController)
        tutorialManageBeneficiaryController.setAdapterCallbacks(this)
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
                viewModel.getBeneficiariesTestData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initRxSearchEventListener()
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchBeneficiaries(true)
            }
        }
        RxView.clicks(imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                editTextSearch.requestFocus()
                editTextSearch.text?.clear()
            }.addTo(disposables)
        RxView.clicks(floatingActionButtonAddBeneficiary)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                val bundle = Bundle()
                bundle.putString(
                    ChannelActivity.EXTRA_PAGE,
                    ChannelActivity.PAGE_BENEFICIARY
                )
                navigator.navigate(
                    this,
                    ChannelActivity::class.java,
                    bundle,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_SELECT_BENEFICIARY_FILTER) {
                currentStatus = it.payload!!
                fetchBeneficiaries(true)
                invalidateOptionsMenu()
            }
        }.addTo(disposables)
        eventBus.actionSyncEvent.flowable.subscribe(
            {
                if (it.eventType == ActionSyncEvent.ACTION_UPDATE_BENEFICIARY_LIST ||
                    it.eventType == ActionSyncEvent.ACTION_DELETE_BENEFICIARY) {
                    getSwipeRefreshLayout().isRefreshing = true
                    fetchBeneficiaries(true)
                }
            }, {
                Timber.d(it, "actionSyncEvent")
            }
        ).addTo(disposables)
    }

    override fun onClickItem(view: View, data: Beneficiary, position: Int) {
        val bundle = Bundle()
        bundle.putString(
            ManageBeneficiaryDetailActivity.EXTRA_ID,
            data.id.toString()
        )
        navigator.navigate(
            this,
            ManageBeneficiaryDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    override fun onTapToRetry() {
        fetchBeneficiaries(isInitialLoading = false, isTapToRetry = true)
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
                            floatingActionButtonAddBeneficiary,
                            R.layout.frame_tutorial_lower_right,
                            floatingActionButtonAddBeneficiary.height.toFloat() -
                                    resources.getDimension(R.dimen.grid_5_half),
                            true,
                            getString(R.string.msg_tutorial_beneficiary_fab),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    floatingActionButtonAddBeneficiary -> {
                        tutorialEngineUtil.startTutorial(
                            this,
                            editTextSearch,
                            R.layout.frame_tutorial_upper_left,
                            resources.getDimension(R.dimen.field_radius_search),
                            false,
                            getString(R.string.msg_tutorial_beneficiary_filter),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    editTextSearch -> {
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
                    getString(R.string.msg_tutorial_beneficiary_item),
                    GravityEnum.BOTTOM,
                    OverlayAnimationEnum.ANIM_EXPLODE
                )
            }
        }
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = true
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
            pageable.isLoadingPagination = false
            if (isTableView()) {
                snackBarProgressBar?.dismiss()
            } else {
                updateController()
            }
        }
    }

    private fun showEmptyState(data: MutableList<Beneficiary>) {
        val isNotEmpty = data.isNotEmpty()
        if (isTableView()) {
            linearLayoutRow.visibility(isNotEmpty)
        }
        if (isNotEmpty) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun updateController(data: MutableList<Beneficiary> = getBeneficiaries()) {
        manageBeneficiaryController.setData(
            data,
            pageable,
            isTableView()
        )
    }

    private fun updateTutorialController(data: MutableList<Beneficiary>) {
        tutorialManageBeneficiaryController.setData(data, pageable, isTableView())
    }

    private fun getBeneficiaries(): MutableList<Beneficiary> {
        return viewModel.beneficiaries.value.notNullable()
    }

    private fun fetchBeneficiaries(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getBeneficiaries(
            pageable = pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(this)
                    fetchBeneficiaries(true)
                    return@OnEditorActionListener true
                }
                false
            }
        )
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
                    pageable.filter = filter.text().toString().nullable()
                    fetchBeneficiaries(true)
                }
            }.addTo(disposables)
    }

    private fun clearTutorial() {
        updateTutorialController(mutableListOf())
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        getRecyclerViewTutorial().setController(tutorialManageBeneficiaryController)
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
                ?.findViewById<CardView>(R.id.cardViewBeneficiary)!!
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().smoothScrollToPosition(0)
        }
    }

    private fun startViewTutorial(data: MutableList<Beneficiary>) {
        getRecyclerView().visibility(false)
        if (isTableView()) {
            linearLayoutRow.visibility(true)
        }
        updateTutorialController(data)
        viewUtil.animateRecyclerView(getRecyclerViewTutorial(), true)
        tutorialEngineUtil.startTutorial(
            this,
            R.drawable.ic_tutorial_beneficiaries,
            getString(R.string.title_beneficiaries),
            getString(R.string.msg_tutorial_beneficiary_landing)
        )
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            swipeRefreshLayoutTable.visibility(true)
            swipeRefreshLayoutBeneficiary.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_manage_beneficiary)
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

    private fun getRecyclerView() =
        if (isTableView()) recyclerViewTable else recyclerViewBeneficiary

    private fun getRecyclerViewTutorial() =
        if (isTableView()) recyclerViewTableTutorial else recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) swipeRefreshLayoutTable else swipeRefreshLayoutBeneficiary

}
