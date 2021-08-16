package com.unionbankph.corporate.app.dashboard.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailActivity
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
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentActivity
import com.unionbankph.corporate.branch.presentation.list.BranchVisitActivity
import com.unionbankph.corporate.common.domain.exception.JsonParseException
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.ebilling.presentation.form.EBillingFormActivity
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
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.swipeRefreshLayoutBtr
import kotlinx.android.synthetic.main.fragment_send_request.*
import timber.log.Timber

class DashboardFragment :
    BaseFragment<DashboardFragmentViewModel>(R.layout.fragment_dashboard),
    DashboardAdapterCallback,
    AccountAdapterCallback{

    private lateinit var settingsViewModel: SettingsViewModel

    private val controller by lazyFast {
        DashboardFragmentController(applicationContext, viewUtil, autoFormatUtil)
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            initRecyclerView()
            initListener()
            initViewModel()

            initSettingsViewModel()
            initActionsSettings()
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
                            viewModel.showDashboardActionItem(
                                Constant.DASHBOARD_ACTION_TRANSFER_FUNDS,
                                it.hasPermission
                            )                         }
                        TransactScreenEnum.BILLS_PAYMENT.name -> {
                            viewModel.showDashboardActionItem(
                                Constant.DASHBOARD_ACTION_PAY_BILLS,
                                it.hasPermission
                            )                        }
                        else -> {
                            viewModel.showDashboardActionItem(
                                Constant.DASHBOARD_ACTION_DEPOSIT_CHECK,
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
                    viewModel.showDashboardActionItem(
                        Constant.DASHBOARD_ACTION_DEPOSIT_CHECK,
                        false
                    )
                }
            } else if (it.first == FeaturesEnum.E_BILLING) {
                viewModel.showDashboardActionItem(
                    Constant.DASHBOARD_ACTION_PAY_BILLS,
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

    private fun initActionsSettings() {
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

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[DashboardFragmentViewModel::class.java]

        viewModel.dashboardViewState.observe(this, Observer {
            controller.setData(it, viewModel.pageable)

            if (swipeRefreshLayoutBtr.isRefreshing) {
                swipeRefreshLayoutBtr.isRefreshing = false
            }
        })

        getAccounts(true)

        setDashboardActionItems()
    }

    private fun setDashboardActionItems() {
        val parseDashboardActions = viewUtil.loadJSONFromAsset(
            getAppCompatActivity(),
            DASHBOARD_ACTIONS
        )
        val dashboardActions = JsonHelper.fromListJson<ActionItem>(parseDashboardActions)
        viewModel.setActionItems(dashboardActions)
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        recyclerViewDashboard.layoutManager = linearLayoutManager
        recyclerViewDashboard.addOnScrollListener(
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
        recyclerViewDashboard.setController(controller)
        controller.setDashboardAdapterCallbacks(this)
        controller.setAccountAdapterCallbacks(this)
    }

    private fun initListener() {
        initSwipeRefreshLayout()
        initEventBus()
        initDataBus()
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayoutBtr.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccounts(true)
            }
        }
    }

    private fun initEventBus() {
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
        Timber.e("Dashboard Item Clicked $id isEnabled $isEnabled")

        when (actionId) {
            Constant.DASHBOARD_ACTION_TRANSFER_FUNDS -> {
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
            Constant.DASHBOARD_ACTION_PAY_BILLS -> {
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
            Constant.DASHBOARD_ACTION_DEPOSIT_CHECK -> {
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
            Constant.DASHBOARD_ACTION_BRANCH_VISIT -> {
                if (isEnabled) {
                    navigator.navigate(
                        (activity as DashboardActivity),
                        BranchVisitActivity::class.java,
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
            Constant.DASHBOARD_ACTION_E_BILLING -> {
                if (isEnabled) {
                    navigator.navigate(
                        (activity as DashboardActivity),
                        EBillingFormActivity::class.java,
                        null,
                        isClear = false,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                } else {
                    showBottomSheetError(
                        formatString(R.string.title_e_billing_unavailable),
                        formatString(R.string.msg_e_billing_unavailable)
                    )
                }
            }
            Constant.DASHBOARD_ACTION_REQUEST_PAYMENT -> {
                if (isEnabled) {
                    eventBus.transactSyncEvent.emmit(
                        BaseEvent(TransactSyncEvent.ACTION_VALIDATE_MERCHANT_EXIST)
                    )
                }
            }
        }
    }

    override fun onTapErrorRetry(id: String, position: Int) {
        viewModel.getCorporateUserAccountDetail(id, position)
    }

    override fun onTapToRetry() {
        getAccounts(isInitialLoading = true, isTapToRetry = true)
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

    companion object {
        const val DASHBOARD_ACTIONS = "dashboard_actions"
    }
}