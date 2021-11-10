package com.unionbankph.corporate.app.dashboard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailActivity
import com.unionbankph.corporate.account.presentation.account_list.AccountFragment
import com.unionbankph.corporate.account.presentation.account_list.AccountsController
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.TransactSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentActivity
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.domain.exception.JsonParseException
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.*
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentDashboardBinding
import com.unionbankph.corporate.fund_transfer.presentation.organization_transfer.OrganizationTransferActivity
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositActivity
import com.unionbankph.corporate.payment_link.presentation.payment_link_list.PaymentLinkListFragment
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsHasPermission
import com.unionbankph.corporate.transact.presentation.transact.TransactFragment
import com.unionbankph.corporate.transact.presentation.transact.TransactScreenEnum
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class DashboardFragment :
    BaseFragment<FragmentDashboardBinding, DashboardFragmentViewModel>(),
    DashboardAdapterCallback,
    AccountAdapterCallback,
    OnTutorialListener {

    private var tutorialDataAccounts: MutableList<Account> = mutableListOf()

    private lateinit var settingsViewModel: SettingsViewModel

    private val isOnTrialMode by lazyFast {
        (activity as DashboardActivity).isOnTrialMode
    }

    private val controller by lazyFast {
        DashboardFragmentController(applicationContext, viewUtil, autoFormatUtil)
    }

    private val tutorialController by lazyFast {
        AccountsController(applicationContext, viewUtil, autoFormatUtil)
    }

    override fun onResume() {
        super.onResume()
        tutorialEngineUtil.setOnTutorialListener(this)
        if (hasInitialLoad) {
            hasInitialLoad = false
            initRecyclerView()
            initListener()
            initTutorialViewModel()
            initViewModel()

            initTrialMode()
            initSettingsViewModel()
            initMenuSettings()
        }
    }

    private fun initSettingsViewModel() {
        settingsViewModel =
            ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        settingsViewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsHasPermission -> {
                    when (it.permissionCode) {
                        TransactScreenEnum.FUND_TRANSFER.name -> {
                            viewModel.enableMenuItem(
                                Constant.MegaMenu.MSME_TRANSFER_FUNDS,
                                it.hasPermission
                            )
                        }
                        TransactScreenEnum.BILLS_PAYMENT.name -> {
                            viewModel.enableMenuItem(
                                Constant.MegaMenu.MSME_PAY_BILLS,
                                it.hasPermission
                            )
                        }
                        else -> {
                            viewModel.enableMenuItem(
                                Constant.MegaMenu.MSME_DEPOSIT_CHECK,
                                it.hasPermission
                            )
                        }
                    }
                }
                is ShowSettingsError -> {
                    if (it.throwable is JsonParseException) {
//                        setCheckDepositClickListener()
                    } else {
                        handleOnError(it.throwable)
                    }
                }
            }
        })
        settingsViewModel.featureToggle.observe(this, Observer {
            val isFeatureEnabled = it.second
            if (it.first == FeaturesEnum.MOBILE_CHECK_DEPOSIT_VIEW) {
                if (isFeatureEnabled) {
                    settingsViewModel.hasPermissionChannel(
                        PermissionNameEnum.CHECK_DEPOSIT.value,
                        Constant.Permissions.CODE_RCD_MOBILE_CHECK
                    )
                } else {
                    viewModel.enableMenuItem(
                        Constant.MegaMenu.MSME_DEPOSIT_CHECK,
                        false
                    )
                }
            } else if (it.first == FeaturesEnum.E_BILLING) {
                viewModel.enableMenuItem(
                    Constant.MegaMenu.MSME_PAY_BILLS,
                    isFeatureEnabled
                )
            }
        })

        eventBus.transactSyncEvent.flowable.subscribe {
            when (it.eventType) {
                TransactSyncEvent.ACTION_REDIRECT_TO_PAYMENT_LINK_LIST -> {
                    navigator.addFragmentWithAnimation(
                        R.id.frameLayoutTransact,
                        PaymentLinkListFragment(),
                        null,
                        childFragmentManager,
                        TransactFragment.FRAGMENT_REQUEST_PAYMENT
                    )
                }
            }
        }.addTo(disposables)
    }

    private fun initMenuSettings() {
        settingsViewModel.hasFundTransferTransactionsPermission(
            TransactScreenEnum.FUND_TRANSFER.name
        )

        if (App.isSupportedInProduction) {
            return
        } else {
            settingsViewModel.hasBillsPaymentTransactionsPermission(
                TransactScreenEnum.BILLS_PAYMENT.name
            )
            settingsViewModel.isEnabledFeature(FeaturesEnum.MOBILE_CHECK_DEPOSIT_VIEW)
        }
    }

    private fun initTrialMode() {
        viewModel.setScreenIsOnTrialMode(isOnTrialMode)
    }

    private fun initViewModel() {

        viewModel.dashboardViewState.observe(this, Observer {
            controller.setData(it, viewModel.pageable)

            if (binding.swipeRefreshLayoutBtr.isRefreshing) {
                binding.swipeRefreshLayoutBtr.isRefreshing = false
            }
        })

        getAccounts(true)

        viewModel.getDashboardMegaMenu()
        viewModel.setDashboardName()
        setMegaMenuItems()
    }

    private fun setMegaMenuItems() {
        val megaMenuItems = GenericMenuItem.generateMegaMenuItems(requireContext())
        viewModel.setMenuItems(megaMenuItems)
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewDashboard.layoutManager = linearLayoutManager
        binding.recyclerViewDashboard.addOnScrollListener(
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
        binding.recyclerViewDashboard.setController(controller)
        binding.recyclerViewTutorialBtr.setController(tutorialController)
        controller.setDashboardAdapterCallbacks(this)
        controller.setAccountAdapterCallbacks(this)
        tutorialController.setAdapterCallbacks(this)
    }

    private fun initListener() {
        initSwipeRefreshLayout()
        initEventBus()
        initDataBus()
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefreshLayoutBtr.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccounts(true)
                viewModel.getDashboardMegaMenu()
            }
        }
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
                    Timber.e("DF ACTION_PUSH_TUTORIAL_ACCOUNT")
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
    }

    private fun initDataBus() {
        dataBus.accountDataBus.flowable.subscribe {
            viewModel.updateEachAccount(it, viewModel.getAccounts())
        }.addTo(disposables)
    }

    private fun getAccounts(
        isInitialLoading: Boolean,
        isTapToRetry: Boolean = false
    ) {
        if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.refreshedLoad()
        } else {
            viewModel.resetLoad()
        }
        viewModel.getCorporateUserOrganization(isInitialLoading)
    }

    override fun onDashboardActionEmit(actionId: String, isEnabled: Boolean) {
        if (isOnTrialMode) return

        when (actionId) {
            Constant.MegaMenu.MSME_TRANSFER_FUNDS -> {
                if (isEnabled) {
                    navigator.navigate(
                        (activity as DashboardActivity),
                        OrganizationTransferActivity::class.java,
                        null,
                        isClear = false,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                } else {
                    showBottomSheetError(
                        formatString(R.string.title_feature_unavailable),
                        formatString(R.string.msg_generic_feature_unavailable)
                    )
                }
            }
            Constant.MegaMenu.MSME_PAY_BILLS -> {
                if (isEnabled) {
                    navigator.navigate(
                        (activity as DashboardActivity),
                        OrganizationPaymentActivity::class.java,
                        null,
                        isClear = false,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                } else {
                    showBottomSheetError(
                        formatString(R.string.title_feature_unavailable),
                        formatString(R.string.msg_generic_feature_unavailable)
                    )
                }
            }
            Constant.MegaMenu.MSME_DEPOSIT_CHECK -> {
                if (isEnabled) {
                    navigator.navigate(
                        (activity as DashboardActivity),
                        CheckDepositActivity::class.java,
                        null,
                        isClear = false,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                } else {
                    showBottomSheetError(
                        formatString(R.string.title_check_deposit_unavailable),
                        formatString(R.string.msg_check_deposit_unavailable)
                    )
                }
            }
            Constant.MegaMenu.MSME_REQUEST_PAYMENT -> {
                eventBus.transactSyncEvent.emmit(
                    BaseEvent(TransactSyncEvent.ACTION_VALIDATE_MERCHANT_EXIST)
                )
            }
            Constant.DASHBOARD_ACTION_VIEW_ALL_ACCOUNTS -> {
                (activity as DashboardActivity).bottomNavigationBTR().currentItem = 1
            }
            Constant.DASHBOARD_ACTION_MORE -> {
                openMenuBottomSheet()
            }
            Constant.DASHBOARD_ACTION_DEFAULT_LOANS -> {
                Timber.e("Default Loans Clicked")
            }
            Constant.DASHBOARD_ACTION_DEFAULT_EARNINGS -> {
                Timber.e("Default Earnings Clicked")
            }
            Constant.Banner.BUSINESS_PROFILE,
            Constant.Banner.LEARN_MORE -> {
                navigator.navigateBrowser(getAppCompatActivity(), URLDataEnum.GLOBALLINKER)
            }
        }
    }

    private fun openMenuBottomSheet() {
        val moreBottomSheet = MegaMenuBottomSheet.newInstance(
            viewModel.dashboardViewState.value?.megaMenuList as ArrayList<GenericMenuItem>
        )
        moreBottomSheet.show(childFragmentManager, "MoreBottomSheet")
    }

    override fun onTapErrorRetry(id: String, position: Int) {
        viewModel.getCorporateUserAccountDetail(id, position)
    }

    override fun onTapToRetry() {
        getAccounts(isInitialLoading = true, isTapToRetry = true)
    }

    override fun onContinueAccountSetup() {
        navigator.navigate(
            getAppCompatActivity(),
            AccountSetupActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
        Timber.e("onContinueAccountSetup clicked")
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

    private fun showBottomSheetError(title: String, message: String) {
        val confirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            title,
            message,
            actionPositive = formatString(R.string.action_okay)
        )
        confirmationBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                super.onClickPositiveButtonDialog(data, tag)
                confirmationBottomSheet.dismiss()
            }
        })
        confirmationBottomSheet.show(
            childFragmentManager,
            this::class.java.simpleName
        )
    }

    fun navigateToPaymentLinkFragment() {
        navigator.addFragmentWithAnimation(
            R.id.frameLayoutTransact,
            PaymentLinkListFragment(),
            null,
            childFragmentManager,
            TransactFragment.FRAGMENT_REQUEST_PAYMENT
        )
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

    private fun startViewTutorial(isShownTestData: Boolean) {
        if ((activity as DashboardActivity).viewPager().currentItem == 0) {
            if (isShownTestData) {
                val parseAccount = viewUtil.loadJSONFromAsset(
                    getAppCompatActivity(),
                    AccountFragment.TEST_DATA_ACCOUNT
                )
                tutorialDataAccounts = JsonHelper.fromListJson(parseAccount)
                tutorialDataAccounts.forEach {
                    it.permissionCollection =
                        PermissionCollection().getPermissionCollectionAllowAll()
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

    private fun clearTutorial() {
        getRecyclerViewTutorial().visibility(false)
        getRecyclerView().visibility(true)
        tutorialDataAccounts.clear()
        updateTutorialController()
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

    private fun getRecyclerView() = binding.recyclerViewDashboard

    private fun getRecyclerViewTutorial() = binding.recyclerViewTutorialBtr

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDashboardBinding
        get() = FragmentDashboardBinding::inflate

    override val viewModelClassType: Class<DashboardFragmentViewModel>
        get() = DashboardFragmentViewModel::class.java
}