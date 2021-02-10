package com.unionbankph.corporate.bills_payment.presentation.organization_payment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
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
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_list.ManageFrequentBillerActivity
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.presentation.channel.ChannelActivity
import com.unionbankph.corporate.general.data.model.TransactionFilterForm
import com.unionbankph.corporate.general.presentation.transaction_filter.TransactionFilterActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_organization_payment.*
import kotlinx.android.synthetic.main.activity_organization_payment.constraintLayout
import kotlinx.android.synthetic.main.activity_organization_payment.recyclerViewTutorial
import kotlinx.android.synthetic.main.activity_organization_payment.swipeRefreshLayoutTable
import kotlinx.android.synthetic.main.activity_organization_payment.textViewState
import kotlinx.android.synthetic.main.activity_organization_payment.viewLoadingState
import kotlinx.android.synthetic.main.activity_organization_payment.viewToolbar
import kotlinx.android.synthetic.main.activity_organization_transfer.*
import kotlinx.android.synthetic.main.widget_badge_small.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_table_view.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import java.util.concurrent.TimeUnit

class OrganizationPaymentActivity :
    BaseActivity<OrganizationPaymentViewModel>(R.layout.activity_organization_payment),
    OnTutorialListener,
    EpoxyAdapterCallback<Transaction> {


    private var snackBarProgressBar: Snackbar? = null

    private val organizationPaymentController by lazyFast {
        OrganizationPaymentController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    private val organizationPaymentTutorialController by lazyFast {
        OrganizationPaymentController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initGeneralViewModel()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
        initRecyclerView()
        initFabButton()
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
        tutorialViewModel.hasTutorial(TutorialScreenEnum.BILLS_PAYMENT)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        getString(R.string.title_bills_payment),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[OrganizationPaymentViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        showEndlessProgressBar()
                    } else {
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
                }
                is UiState.Complete -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        dismissEndlessProgressBar()
                    } else {
                        dismissLoading(
                            viewLoadingState,
                            getSwipeRefreshLayout(),
                            getRecyclerView()
                        )
                    }
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.transactions.observe(this, Observer {
            it?.let {
                updateController(it)
                if (viewModel.pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        viewModel.testTransactions.observe(this, Observer {
            it?.let {
                startViewTutorial(false)
            }
        })
        fetchTransactionHistory(true)
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
                if (fabBillsPayment.isOpened) {
                    fabBillsPayment.toggle(true)
                }
                isClickedHelpTutorial = true
                viewModel.getOrganizationPaymentsTestData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (fabBillsPayment.isOpened) {
            fabBillsPayment.toggle(true)
        } else {
            onBackPressed(false)
            overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initRxSearchEventListener()
        tutorialEngineUtil.setOnTutorialListener(this)
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchTransactionHistory(true)
            }
        }
        RxView.clicks(imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                editTextSearch.text?.clear()
            }.addTo(disposables)
        RxView.clicks(imageViewFilter)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateTransactionFilterScreen()
                if (fabBillsPayment.isOpened) {
                    fabBillsPayment.toggle(true)
                }
            }.addTo(disposables)
    }

    private fun init() {
        imageViewFilter.visibility(true)
    }

    private fun initBinding() {
        viewModel.hasFilter
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (!it) {
                    viewModel.transactionFilterForm.onNext(TransactionFilterForm())
                }
                fetchTransactionHistory(true)
                invalidateOptionsMenu()
            }.addTo(disposables)
        viewModel.transactionFilterForm
            .subscribe {
                it.count?.let {
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
                if (it.count == null) {
                    imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                    viewBadgeCount.visibility(false)
                }
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            when {
                it.eventType == ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST -> {
                    if (viewLoadingState.visibility != View.VISIBLE) {
                        getSwipeRefreshLayout().isRefreshing = true
                    }
                    fetchTransactionHistory(true)
                }
                it.eventType == ActionSyncEvent.ACTION_APPLY_FILTER_FUND_TRANSFER -> {
                    val transactionFilterForm =
                        JsonHelper.fromJson<TransactionFilterForm>(it.payload)
                    viewModel.hasFilter.onNext(true)
                    viewModel.transactionFilterForm.onNext(transactionFilterForm)
                }
                it.eventType == ActionSyncEvent.ACTION_CLEAR_FILTER -> {
                    viewModel.hasFilter.onNext(false)
                }
            }
        }.addTo(disposables)
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener
        { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this@OrganizationPaymentActivity)
                fetchTransactionHistory(true)
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
                    fetchTransactionHistory(true)
                }
            }
            .addTo(disposables)
    }

    override fun onClickItem(view: View, data: Transaction, position: Int) {
        if (data.ongoing == true) {
            showProcessingStatusRestrictionDialog()
        } else {
            navigateApprovalDetails(data)
        }
    }

    override fun onTapToRetry() {
        fetchTransactionHistory(isInitialLoading = false, isTapToRetry = true)
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
                            fabBillsPayment.menuIconView,
                            R.layout.frame_tutorial_lower_right,
                            fabBillsPayment.menuIconView.height.toFloat() +
                                    resources.getDimension(R.dimen.content_spacing),
                            true,
                            getString(R.string.msg_tutorial_bills_payment_create),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    fabBillsPayment.menuIconView -> {
                        tutorialEngineUtil.startTutorial(
                            this,
                            imageViewFilter,
                            R.layout.frame_tutorial_upper_right,
                            getCircleFloatSize(imageViewFilter),
                            true,
                            getString(R.string.msg_tutorial_bills_payment_filter),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    imageViewFilter -> {
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

    private fun getCircleFloatSize(view: View): Float {
        return view.height.toFloat() - resources.getDimension(R.dimen.view_tutorial_radius_padding)
    }

    private fun navigateTransactionFilterScreen() {
        val bundle = Bundle().apply {
            putString(
                TransactionFilterActivity.EXTRA_SCREEN,
                TransactionFilterActivity.BILLS_PAYMENT_SCREEN
            )
            if (viewModel.hasFilter.value == true) {
                viewModel.transactionFilterForm.value?.let {
                    putString(
                        TransactionFilterActivity.EXTRA_TRANSACTION_FILTER_FORM,
                        JsonHelper.toJson(it)
                    )
                }
            }
        }
        navigator.navigate(
            this,
            TransactionFilterActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initFabButton() {
        val set = AnimatorSet()
        val scaleOutX = ObjectAnimator.ofFloat(fabBillsPayment.menuIconView, "scaleX", 1.0f, 0.2f)
        val scaleOutY = ObjectAnimator.ofFloat(fabBillsPayment.menuIconView, "scaleY", 1.0f, 0.2f)
        val scaleInX = ObjectAnimator.ofFloat(fabBillsPayment.menuIconView, "scaleX", 0.2f, 1.0f)
        val scaleInY = ObjectAnimator.ofFloat(fabBillsPayment.menuIconView, "scaleY", 0.2f, 1.0f)
        scaleOutX.duration = 50
        scaleOutY.duration = 50
        scaleInX.duration = 150
        scaleInY.duration = 150
        set.play(scaleOutX).with(scaleOutY)
        set.play(scaleInX).with(scaleInY).after(scaleOutX)
        set.interpolator = OvershootInterpolator(2f)
        fabBillsPayment.menuIconView.id = R.id.imageViewMakeAPayment
        fabBillsPayment.iconToggleAnimatorSet = set
        fabBillsPayment.setClosedOnTouchOutside(true)
        initFabButtonListener(scaleInX)
    }

    private fun initFabButtonListener(scaleInX: ObjectAnimator) {
        scaleInX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                if (fabBillsPayment.isOpened) {
                    fabBillsPayment.menuIconView.setImageResource(R.drawable.ic_plus_white)
                } else {
                    initMakePaymentFab()
                }
            }
        })
        fabBillsPayment.setOnMenuButtonClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                return@setOnMenuButtonClickListener
            if (fabBillsPayment.isOpened) {
                navigateChannelScreen()
            } else {
                fabBillsPayment.toggle(true)
            }
        }
        fabMenuManageFrequentBillers.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            fabBillsPayment.toggle(true)
            navigateFrequentBillerScreen()
        }
    }

    private fun initMakePaymentFab() {
        fabBillsPayment.menuButtonLabelText = getString(R.string.title_make_payment)
        fabBillsPayment.menuIconView.setImageResource(R.drawable.ic_card_white)
    }

    private fun navigateChannelScreen() {
        if (!viewModel.hasCreateTransaction.value.notNullable()) {
            showPermissionAccountBottomSheet()
        } else {
            fabBillsPayment.toggle(false)
            val bundle = Bundle()
            bundle.putString(ChannelActivity.EXTRA_PAGE, ChannelActivity.PAGE_BILLS_PAYMENT)
            navigator.navigate(
                this,
                ChannelActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
            )
        }
    }

    private fun navigateFrequentBillerScreen() {
        navigator.navigate(
            this,
            ManageFrequentBillerActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun navigateApprovalDetails(data: Transaction) {
        val bundle = Bundle()
        bundle.putString(
            ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL,
            JsonHelper.toJson(data)
        )
        navigator.navigate(
            this,
            ApprovalDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun fetchTransactionHistory(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getOrganizationPayments(
            pageable = viewModel.pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun updateController(data: MutableList<Transaction> = getTransactionsLiveData()) {
        organizationPaymentController.setData(data, viewModel.pageable, isTableView())
    }

    private fun updateTutorialController() {
        organizationPaymentTutorialController.setData(
            getTestTransactionsLiveData(),
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
        getTestTransactionsLiveData().clear()
        updateTutorialController()
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        getRecyclerViewTutorial().setController(organizationPaymentTutorialController)
        if (viewLoadingState.visibility != View.VISIBLE) {
            showEmptyState(getTransactionsLiveData())
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

    private fun showEmptyState(data: MutableList<Transaction>) {
        if (isTableView()) {
            linearLayoutRow.visibility(data.isNotEmpty())
        }
        if (data.isNotEmpty()) {
            if (textViewState?.visibility == View.VISIBLE) textViewState?.visibility = View.GONE
        } else {
            textViewState?.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
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

    private fun getTransactionsLiveData(): MutableList<Transaction> {
        return viewModel.transactions.value.notNullable()
    }

    private fun getTestTransactionsLiveData(): MutableList<Transaction> {
        return viewModel.testTransactions.value.notNullable()
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
                    if (!viewModel.pageable.isLoadingPagination) fetchTransactionHistory(false)
                }
            }
        )
        getRecyclerView().setController(organizationPaymentController)
        organizationPaymentController.setAdapterCallbacks(this)

        getRecyclerViewTutorial().setController(organizationPaymentTutorialController)
        organizationPaymentTutorialController.setAdapterCallbacks(this)
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            swipeRefreshLayoutTable.visibility(true)
            swipeRefreshLayoutOrganizationPayment.visibility(false)
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

    private fun getRecyclerView() =
        if (isTableView()) recyclerViewTable else recyclerViewOrganizationPayment

    private fun getRecyclerViewTutorial() =
        if (isTableView()) recyclerViewTableTutorial else recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) swipeRefreshLayoutTable else swipeRefreshLayoutOrganizationPayment

    private fun showProcessingStatusRestrictionDialog() {
        val processingStatusRestrictionDialog = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            getString(R.string.title_transaction_unavailable),
            getString(R.string.msg_transaction_unavailable),
            null,
            getString(R.string.action_close)
        )
        processingStatusRestrictionDialog.isCancelable = false
        processingStatusRestrictionDialog.setOnConfirmationPageCallBack(object :
            OnConfirmationPageCallBack {
            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                super.onClickNegativeButtonDialog(data, tag)
                processingStatusRestrictionDialog.dismiss()
            }
        })
        processingStatusRestrictionDialog.show(
            supportFragmentManager,
            this::class.java.simpleName
        )
    }

    private fun showPermissionAccountBottomSheet() {
        val bottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_no_permission_to_transact),
            formatString(R.string.msg_no_permission_to_transact),
            actionNegative = formatString(R.string.action_close)
        )
        bottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                bottomSheet.dismiss()
            }
        })
        bottomSheet.show(
            supportFragmentManager,
            this::class.java.simpleName
        )
    }

}
