package com.unionbankph.corporate.account.presentation.account_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentAccountsBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class AccountFragment :
    BaseFragment<FragmentAccountsBinding, AccountViewModel>(),
    AccountAdapterCallback,
    OnTutorialListener {

    private var tutorialDataAccounts: MutableList<Account> = mutableListOf()

    private val controller by lazyFast {
        AccountsController(applicationContext, viewUtil, autoFormatUtil)
    }

    private val tutorialController by lazyFast {
        AccountsController(applicationContext, viewUtil, autoFormatUtil)
    }

    private var snackBarProgressBar: Snackbar? = null

    override fun onResume() {
        super.onResume()
        tutorialEngineUtil.setOnTutorialListener(this)
        if (hasInitialLoad) {
            hasInitialLoad = false
            init()
            initListener()
            initTutorialViewModel()
            initViewModel()
        }
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowAccountLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        showEndlessProgressBar()
                    } else {
                        showLoading(
                            binding.viewLoadingState.viewLoadingLayout,
                            getSwipeRefreshLayout(),
                            getRecyclerView(),
                            binding.textViewState,
                            binding.linearLayoutRow
                        )
                        if (binding.viewLoadingState.viewLoadingLayout.visibility == View.VISIBLE) {
                            updateController(mutableListOf())
                        }
                    }
                }
                is ShowAccountDismissLoading -> {
                    if (!viewModel.pageable.isInitialLoad) {
                        dismissEndlessProgressBar()
                    } else {
                        dismissLoading(
                            binding.viewLoadingState.viewLoadingLayout,
                            getSwipeRefreshLayout(),
                            getRecyclerView()
                        )
                    }
                }
                is ShowAccountLoadingAccountDetail -> {
                    updateController()
                }
                is ShowAccountDismissLoadingAccountDetail -> {
                    updateController()
                }
                is ShowAccountDetailError,
                is ShowAccountsDetailError -> {
                    updateController()
                }
            }
            if ((activity as DashboardActivity).viewPager().currentItem == 0) {
                when (it) {
                    is ShowAccountError -> {
                        handleOnError(it.throwable)
                    }
                }
            }
        })
        viewModel.accounts.observe(this, Observer {
            updateController(it)
            if (viewModel.pageable.isInitialLoad) {
                showEmptyState(it)
                scrollToTop()
            }
        })
        getAccounts(true)
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
                        startViewTutorial(false)
                    } else {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun init() {
        initRecyclerView()
    }

    private fun initListener() {
        initRxSearchEventListener()
        initEventBus()
        initDataBus()
        initSwipeRefreshLayout()
    }

    private fun initSwipeRefreshLayout() {
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccounts(true)
            }
        }
    }

    private fun initDataBus() {
        dataBus.accountDataBus.flowable.subscribe {
            viewModel.updateEachAccount(it, viewModel.getAccounts())
        }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_TUTORIAL_ACCOUNT -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                    )
                    tutorialViewModel.hasTutorial(TutorialScreenEnum.ACCOUNTS)
                }
                SettingsSyncEvent.ACTION_TUTORIAL_BOTTOM,
                SettingsSyncEvent.ACTION_RESET_TUTORIAL,
                SettingsSyncEvent.ACTION_SKIP_TUTORIAL ->
                    clearTutorial()
                SettingsSyncEvent.ACTION_SCROLL_TO_TOP ->
                    if ((activity as DashboardActivity).viewPager().currentItem == 0) {
                        getRecyclerView().smoothScrollToPosition(0)
                    }
                SettingsSyncEvent.ACTION_PUSH_TUTORIAL_ACCOUNT -> {
                    isClickedHelpTutorial = true
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                    )
                    startViewTutorial(true)
                }
            }
        }.addTo(disposables)
        eventBus.accountSyncEvent.flowable.subscribe { eventBus ->
            if (eventBus.eventType == AccountSyncEvent.ACTION_UPDATE_CURRENT_BALANCE) {
                eventBus.payload?.let { account ->
                    val currentAccounts = viewModel.getAccounts()
                    Observable.fromIterable(currentAccounts)
                        .takeWhile { it.id != account.id }
                        .count()
                        .map { if (it == currentAccounts.size.toLong()) -1 else it }
                        .subscribe { position ->
                            viewModel.getCorporateUserAccountDetail(
                                account.id.toString(),
                                position.toInt()
                            )
                        }
                }
            }
        }.addTo(disposables)
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_RESET_LIST_UI) {
                resetUIList()
            }
        }.addTo(disposables)
    }

    private fun initRxSearchEventListener() {
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.viewSearchLayout.editTextSearch.clearFocus()
                viewUtil.dismissKeyboard(getAppCompatActivity())
                getAccounts(true)
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
                    getAccounts(true)
                }
            }
            .addTo(disposables)
    }

    override fun onTapErrorRetry(id: String, position: Int) {
        viewModel.getCorporateUserAccountDetail(id, position)
    }

    override fun onTapToRetry() {
        getAccounts(isInitialLoading = false, isTapToRetry = true)
    }

    override fun onClickItem(account: String, position: Int) {
        val bundle = Bundle()
        bundle.putString(AccountDetailActivity.EXTRA_ACCOUNT, account)
        navigator.navigate(
            getAppCompatActivity(),
            AccountDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun updateController(
        data: MutableList<Account> = viewModel.getAccounts()
    ) {
        controller.setData(data, viewModel.pageable, isTableView())
    }

    private fun updateTutorialController() {
        tutorialController.setData(tutorialDataAccounts, viewModel.pageable, isTableView())
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
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
            )
        } else {
            if (view != null) {
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_ACCOUNT_TAP)
                )
            } else {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    tutorialEngineUtil.startTutorial(
                        getAppCompatActivity(),
                        getRecyclerViewTutorial()
                            .findViewHolderForAdapterPosition(0)
                            ?.itemView!!,
                        R.layout.frame_tutorial_upper_left,
                        if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
                        false,
                        getString(R.string.msg_tutorial_account_sample),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                } else {
                    tutorialViewModel.setTutorial(TutorialScreenEnum.ACCOUNTS, false)
                    clearTutorial()
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                    )
                }
            }
        }
    }

    private fun showEmptyState(data: MutableList<Account> = viewModel.getAccounts()) {
        if (isTableView()) {
            binding.linearLayoutRow.visibility(data.isNotEmpty())
        }
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
    }

    private fun clearTutorial() {
        getRecyclerViewTutorial().visibility(false)
        getRecyclerView().visibility(true)
        tutorialDataAccounts.clear()
        updateTutorialController()
        if (binding.viewLoadingState.viewLoadingLayout.visibility != View.VISIBLE) {
            showEmptyState()
        }
    }

    private fun startViewTutorial(isShownTestData: Boolean) {
        if ((activity as DashboardActivity).viewPager().currentItem == 1) {
            if (isShownTestData) {
                val parseAccount = viewUtil.loadJSONFromAsset(
                    getAppCompatActivity(),
                    TEST_DATA_ACCOUNT
                )
                tutorialDataAccounts = JsonHelper.fromListJson(parseAccount)
                tutorialDataAccounts.forEach {
                    it.permissionCollection =
                        PermissionCollection().getPermissionCollectionAllowAll()
                }
                if (isTableView()) {
                    binding.linearLayoutRow.visibility(true)
                }
                getRecyclerView().visibility(false)
                updateTutorialController()
                viewUtil.animateRecyclerView(getRecyclerViewTutorial(), true)
            }
            tutorialEngineUtil.startTutorial(
                getAppCompatActivity(),
                R.drawable.ic_tutorial_accounts_orange,
                getString(R.string.title_tab_accounts),
                getString(R.string.msg_tutorial_account)
            )
        }
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutBtr.visibility(false)
            val headers = resources.getStringArray(R.array.array_headers_accounts).toMutableList()
            binding.linearLayoutRow.removeAllViews()
            binding.linearLayoutRow.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                val linearLayoutHeaderRow =
                    viewRowHeader.findViewById<LinearLayout>(R.id.linearLayoutHeaderRow)
                val layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f
                linearLayoutHeaderRow.layoutParams = layoutParams
                textViewHeader.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                textViewHeader.text = it
                binding.linearLayoutRow.addView(viewRowHeader)
            }
        }
    }

    private fun resetUIList() {
        if (!hasInitialLoad) {
            clearRecyclerView()
            initSwipeRefreshLayout()
            initRecyclerView()
            showEmptyState()
            updateController()
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

    private fun clearRecyclerView() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutBtr.visibility(false)
            binding.recyclerViewBtr.clear()
            binding.recyclerViewBtr.clearPreloaders()
            binding.recyclerViewTutorialBtr.clear()
            binding.recyclerViewTutorialBtr.clearPreloaders()
            binding.recyclerViewBtr.adapter = null
            binding.recyclerViewTutorialBtr.adapter = null
        } else {
            binding.swipeRefreshLayoutTable.visibility(false)
            binding.swipeRefreshLayoutBtr.visibility(true)
            binding.recyclerViewTable.clear()
            binding.recyclerViewTable.clearPreloaders()
            binding.recyclerViewTableTutorial.clear()
            binding.recyclerViewTableTutorial.clearPreloaders()
            binding.recyclerViewTable.adapter = null
            binding.recyclerViewTableTutorial.adapter = null
        }
    }

    private fun getRecyclerView() = if (isTableView()) binding.recyclerViewTable else binding.recyclerViewBtr

    private fun getRecyclerViewTutorial() =
        if (isTableView()) binding.recyclerViewTableTutorial else binding.recyclerViewTutorialBtr

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutBtr

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
                    if (!viewModel.pageable.isLoadingPagination) getAccounts(false)
                }
            }
        )
        getRecyclerView().setController(controller)
        getRecyclerViewTutorial().setController(tutorialController)
        controller.setAdapterCallbacks(this)
        tutorialController.setAdapterCallbacks(this)
    }

    private fun getAccounts(
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
        viewModel.getCorporateUserOrganization(isInitialLoading)
    }

    companion object {

        const val TEST_DATA_ACCOUNT: String = "accounts"

    }

    override val viewModelClassType: Class<AccountViewModel>
        get() = AccountViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAccountsBinding
        get() = FragmentAccountsBinding::inflate
}
