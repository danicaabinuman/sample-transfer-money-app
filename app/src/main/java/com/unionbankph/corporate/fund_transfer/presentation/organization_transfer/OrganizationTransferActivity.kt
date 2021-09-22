package com.unionbankph.corporate.fund_transfer.presentation.organization_transfer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
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
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
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
import com.unionbankph.corporate.databinding.ActivityOrganizationTransferBinding
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list.ManageBeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ManageScheduledTransferActivity
import com.unionbankph.corporate.general.data.model.TransactionFilterForm
import com.unionbankph.corporate.general.presentation.transaction_filter.TransactionFilterActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class OrganizationTransferActivity :
    BaseActivity<ActivityOrganizationTransferBinding, OrganizationTransferViewModel>(),
    OnTutorialListener,
    EpoxyAdapterCallback<Transaction>, OnConfirmationPageCallBack {

    private var snackBarProgressBar: Snackbar? = null

    private val organizationTransferController by lazyFast {
        OrganizationTransferController(
            this,
            viewUtil,
            autoFormatUtil
        )
    }

    private val organizationTransferTutorialController by lazyFast {
        OrganizationTransferController(
            this,
            viewUtil,
            autoFormatUtil
        )
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
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
        tutorialViewModel.hasTutorial(TutorialScreenEnum.FUND_TRANSFER)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        getString(R.string.title_fund_transfer),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        showEndlessProgressBar()
                    } else {
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
                }
                is UiState.Complete -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        dismissEndlessProgressBar()
                    } else {
                        dismissLoading(
                            binding.viewLoadingState.root,
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
        viewModel.onlyCreateTransaction.observe(this, Observer {
            if (it) {
                binding.fabFundTransfer.menuIconView.setImageResource(R.drawable.ic_card_white)
            } else {
                binding.fabFundTransfer.menuIconView.setImageResource(R.drawable.ic_plus_white)
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
                if (binding.fabFundTransfer.isOpened) {
                    binding.fabFundTransfer.toggle(true)
                }
                isClickedHelpTutorial = true
                viewModel.getOrganizationTransfersTestData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.fabFundTransfer.isOpened) {
            binding.fabFundTransfer.toggle(true)
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
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)

        RxView.clicks(binding.viewSearchLayout.imageViewFilter)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateTransactionFilterScreen()
                if (binding.fabFundTransfer.isOpened) {
                    binding.fabFundTransfer.toggle(true)
                }
            }.addTo(disposables)
    }

    private fun init() {
        binding.viewSearchLayout.imageViewFilter.visibility(true)
    }

    private fun initBinding() {
        viewModel.hasFilter
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (!it) {
                    viewModel.transactionFilterForm.onNext(TransactionFilterForm())
                }
                fetchTransactionHistory(true)
            }.addTo(disposables)
        viewModel.transactionFilterForm
            .subscribe {
                it.count?.let {
                    val hasBadge = it > 0
                    binding.viewSearchLayout.viewBadgeCount.root.visibility(hasBadge)
                    binding.viewSearchLayout.viewBadgeCount.textViewBadgeCount.text = it.toString()
                    binding.viewSearchLayout.viewBadgeCount.textViewBadgeCount
                        .setContextCompatBackground(R.drawable.circle_solid_orange)
                    if (hasBadge) {
                        binding.viewSearchLayout.imageViewFilter.setImageResource(R.drawable.ic_filter_orange)
                    } else {
                        binding.viewSearchLayout.imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                    }
                }
                if (it.count == null) {
                    binding.viewSearchLayout.imageViewFilter.setImageResource(R.drawable.ic_filter_gray)
                    binding.viewSearchLayout.viewBadgeCount.root.visibility(false)
                }
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            when (it.eventType) {
                ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST -> {
                    if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
                        getSwipeRefreshLayout().isRefreshing = true
                    }
                    fetchTransactionHistory(true)
                }
                ActionSyncEvent.ACTION_APPLY_FILTER_FUND_TRANSFER -> {
                    val transactionFilterForm =
                        JsonHelper.fromJson<TransactionFilterForm>(it.payload)
                    transactionFilterForm.count?.let {
                        if (it > 0) {
                            viewModel.hasFilter.onNext(true)
                        }
                    }
                    viewModel.transactionFilterForm.onNext(transactionFilterForm)
                }
                ActionSyncEvent.ACTION_CLEAR_FILTER -> {
                    viewModel.hasFilter.onNext(false)
                }
            }
        }.addTo(disposables)
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch
            .setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.viewSearchLayout.editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(this@OrganizationTransferActivity)
                fetchTransactionHistory(true)
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
                    fetchTransactionHistory(true)
                }
            }
            .addTo(disposables)
    }

    override fun onClickItem(view: View, data: Transaction, position: Int) {
        if (data.ongoing == true) {
            showProcessingStatusRestrictionDialog()
        } else {
            navigateApprovalDetailScreen(data)
        }
    }

    override fun onTapToRetry() {
        fetchTransactionHistory(isInitialLoading = false, isTapToRetry = true)
    }

    private fun navigateApprovalDetailScreen(data: Transaction) {
        val bundle = Bundle().apply {
            putString(
                ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL,
                JsonHelper.toJson(data)
            )
        }
        navigator.navigate(
            this,
            ApprovalDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
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
                            binding.fabFundTransfer.menuIconView,
                            R.layout.frame_tutorial_lower_right,
                            binding.fabFundTransfer.menuIconView.height.toFloat() +
                                    resources.getDimension(R.dimen.content_spacing),
                            true,
                            getString(R.string.msg_tutorial_fund_transfer_create),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    binding.fabFundTransfer.menuIconView -> {
                        tutorialEngineUtil.startTutorial(
                            this,
                            binding.viewSearchLayout.imageViewFilter,
                            R.layout.frame_tutorial_upper_right,
                            getCircleFloatSize(binding.viewSearchLayout.imageViewFilter),
                            true,
                            getString(R.string.msg_tutorial_fund_transfer_filter),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    binding.viewSearchLayout.imageViewFilter -> {
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
                        getString(R.string.msg_tutorial_fund_transfer_sample),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                } else {
                    clearTutorial()
                    tutorialViewModel.setTutorial(TutorialScreenEnum.FUND_TRANSFER, false)
                }
            }
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
                ?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewContent)!!
        }
    }

    private fun getCircleFloatSize(view: View): Float {
        return view.height.toFloat() - resources.getDimension(R.dimen.view_tutorial_radius_padding)
    }

    private fun initFabButton() {
        val set = AnimatorSet()
        val scaleOutX = ObjectAnimator.ofFloat(binding.fabFundTransfer.menuIconView, "scaleX", 1.0f, 0.2f)
        val scaleOutY = ObjectAnimator.ofFloat(binding.fabFundTransfer.menuIconView, "scaleY", 1.0f, 0.2f)
        val scaleInX = ObjectAnimator.ofFloat(binding.fabFundTransfer.menuIconView, "scaleX", 0.2f, 1.0f)
        val scaleInY = ObjectAnimator.ofFloat(binding.fabFundTransfer.menuIconView, "scaleY", 0.2f, 1.0f)
        scaleOutX.duration = 50
        scaleOutY.duration = 50
        scaleInX.duration = 150
        scaleInY.duration = 150
        set.play(scaleOutX).with(scaleOutY)
        set.play(scaleInX).with(scaleInY).after(scaleOutX)
        set.interpolator = OvershootInterpolator(2f)
        binding.fabFundTransfer.menuIconView.id = R.id.imageViewMakeATransfer
        binding.fabFundTransfer.iconToggleAnimatorSet = set
        binding.fabFundTransfer.setClosedOnTouchOutside(true)
        initFabButtonListener(scaleInX)
    }

    private fun initFabButtonListener(scaleInX: ObjectAnimator) {
        scaleInX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                if (binding.fabFundTransfer.isOpened) {
                    binding.fabFundTransfer.menuIconView.setImageResource(R.drawable.ic_plus_white)
                } else {
                    initMakeTransferFab()
                    if (!viewModel.hasViewBeneficiaryPermission.value.notNullable()) {
                        binding.fabMenuManageBeneficiaries.labelVisibility = View.GONE
                        binding.fabMenuManageBeneficiaries.visibility = View.GONE
                    }
                    if (!viewModel.hasScheduledTransferPermission.value.notNullable()) {
                        binding.fabMenuManageScheduledTransfer.labelVisibility = View.GONE
                        binding.fabMenuManageScheduledTransfer.visibility = View.GONE
                    }
                }
            }
        })
        binding.fabFundTransfer.setOnMenuButtonClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                return@setOnMenuButtonClickListener
            if (binding.fabFundTransfer.isOpened) {
                navigateChannelBankScreen()
            } else {
                if (viewModel.onlyCreateTransaction.value.notNullable()) {
                    navigateChannelBankScreen()
                } else {
                    binding.fabFundTransfer.toggle(true)
                }
            }
        }
        binding.fabMenuManageBeneficiaries.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            navigateManageBeneficiaryScreen()
            binding.fabFundTransfer.toggle(true)
        }
        binding.fabMenuManageScheduledTransfer.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            navigateManageScheduledTransferScreen()
            binding.fabFundTransfer.toggle(true)
        }
    }

    private fun initMakeTransferFab() {
        binding.fabFundTransfer.menuButtonLabelText = getString(R.string.title_make_transfer)
        binding.fabFundTransfer.menuIconView.setImageResource(R.drawable.ic_card_white)
    }

    private fun navigateManageScheduledTransferScreen() {
        navigator.navigate(
            this,
            ManageScheduledTransferActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun navigateTransactionFilterScreen() {
        val bundle = Bundle().apply {
            putString(
                TransactionFilterActivity.EXTRA_SCREEN,
                TransactionFilterActivity.FUND_TRANSFER_SCREEN
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

    private fun navigateManageBeneficiaryScreen() {
        navigator.navigate(
            this,
            ManageBeneficiaryActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }


    private fun fetchTransactionHistory(
        isInitialLoading: Boolean,
        isTapToRetry: Boolean = false
    ) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.pageable.resetPagination()
        } else {
            viewModel.pageable.resetLoad()
        }
        viewModel.getOrganizationTransfers(isInitialLoading)
    }

    private fun navigateChannelBankScreen() {
        if (!viewModel.hasCreateTransaction.value.notNullable()) {
            showPermissionAccountBottomSheet()
        } else {
            binding.fabFundTransfer.toggle(false)
            val bundle = Bundle()
            bundle.putString(ChannelActivity.EXTRA_PAGE, ChannelActivity.PAGE_FUND_TRANSFER)
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

    private fun updateController(data: MutableList<Transaction> = getTransactionsLiveData()) {
        organizationTransferController.setData(data, viewModel.pageable, isTableView())
    }

    private fun updateTutorialController() {
        organizationTransferTutorialController.setData(
            getTestTransactionsLiveData(),
            viewModel.pageable,
            isTableView()
        )
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

    private fun showEmptyState(data: MutableList<Transaction>) {
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(data.size > 0)
        }
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
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
        getRecyclerView().setController(organizationTransferController)
        organizationTransferController.setAdapterCallbacks(this)

        getRecyclerViewTutorial().setController(organizationTransferTutorialController)
        organizationTransferTutorialController.setAdapterCallbacks(this)
    }

    private fun clearTutorial() {
        getTestTransactionsLiveData().clear()
        updateTutorialController()
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        getRecyclerViewTutorial().setController(organizationTransferTutorialController)
        if (binding.viewLoadingState.root.visibility != View.VISIBLE) {
            showEmptyState(getTransactionsLiveData())
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
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
            R.drawable.ic_tutorial_fund_transfer_orange,
            getString(R.string.title_fund_transfer),
            getString(R.string.msg_tutorial_fund_transfer)
        )
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutOrganizationTransfer.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_organization_transfers)
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
        if (isTableView())
            binding.viewTable.recyclerViewTable
        else
            binding.recyclerViewOrganizationTransfer

    private fun getRecyclerViewTutorial() =
        if (isTableView())
            binding.viewTable.recyclerViewTableTutorial
        else
            binding.recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView())
            binding.swipeRefreshLayoutTable
        else
            binding.swipeRefreshLayoutOrganizationTransfer

    private fun showProcessingStatusRestrictionDialog() {
        val processingStatusRestrictionDialog = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            getString(R.string.title_transaction_unavailable),
            getString(R.string.msg_transaction_unavailable),
            null,
            getString(R.string.action_close)
        )
        processingStatusRestrictionDialog.isCancelable = false
        processingStatusRestrictionDialog.setOnConfirmationPageCallBack(
            object : OnConfirmationPageCallBack {
                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    super.onClickNegativeButtonDialog(data, tag)
                    processingStatusRestrictionDialog.dismiss()
                }
            }
        )
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

    override val viewModelClassType: Class<OrganizationTransferViewModel>
        get() = OrganizationTransferViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityOrganizationTransferBinding
        get() = ActivityOrganizationTransferBinding::inflate

}
