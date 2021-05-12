package com.unionbankph.corporate.transact.presentation.transact

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.setEnableView
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.TransactSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentActivity
import com.unionbankph.corporate.branch.presentation.list.BranchVisitActivity
import com.unionbankph.corporate.common.domain.exception.JsonParseException
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.ebilling.presentation.form.EBillingFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.organization_transfer.OrganizationTransferActivity
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositActivity
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsHasPermission
import com.unionbankph.corporate.payment_link.presentation.payment_link_list.PaymentLinkListFragment
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_send_request.*

class TransactFragment :
    BaseFragment<SettingsViewModel>(R.layout.fragment_send_request), OnTutorialListener {

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            initTutorialViewModel()
            initViewModel()
            init()
            initListener()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as DashboardActivity).setToolbarTitle(
                getString(R.string.title_dashboard_header_transact),
                hasBackButton = false,
                hasMenuItem = true
        )

    }
    private fun init() {
        if (App.isSupportedInProduction) {
            constraintLayoutBranchVisit.visibility(false)
            view5.visibility(false)
            constraintLayoutElectronicBilling.visibility(false)
            view7.visibility(false)
        }
        viewModel.hasFundTransferTransactionsPermission(
            TransactScreenEnum.FUND_TRANSFER.name
        )
        viewModel.hasBillsPaymentTransactionsPermission(
            TransactScreenEnum.BILLS_PAYMENT.name
        )
        viewModel.isEnabledFeature(FeaturesEnum.MOBILE_CHECK_DEPOSIT_VIEW)
        //viewModel.isEnabledFeature(FeaturesEnum.E_BILLING)
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
                        startTransactTutorial()
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

    private fun initListener() {
        tutorialEngineUtil.setOnTutorialListener(this)
        initEventBus()
        initClickListener()
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
        if (!isSkipTutorial) {
            if (view != null) {
                when (view.id) {
                    constraintLayoutFundTransfer.id -> {
                        tutorialEngineUtil.startTutorial(
                            getAppCompatActivity(),
                            constraintLayoutBillsPayment,
                            R.layout.frame_tutorial_upper_left,
                            0f,
                            false,
                            getString(R.string.msg_tutorial_transact_bills_payment),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    constraintLayoutBillsPayment.id -> {
                        tutorialEngineUtil.startTutorial(
                            getAppCompatActivity(),
                            constraintLayoutCheckDeposit,
                            R.layout.frame_tutorial_upper_left,
                            0f,
                            false,
                            getString(R.string.msg_tutorial_transact_check_deposit),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    constraintLayoutCheckDeposit.id -> {
                        if (constraintLayoutElectronicBilling.visibility == View.VISIBLE) {
                            tutorialEngineUtil.startTutorial(
                                getAppCompatActivity(),
                                constraintLayoutElectronicBilling,
                                R.layout.frame_tutorial_upper_left,
                                0f,
                                false,
                                getString(R.string.msg_tutorial_transact_electronic_billing),
                                GravityEnum.BOTTOM,
                                OverlayAnimationEnum.ANIM_EXPLODE
                            )
                        }
                    }
                    constraintLayoutElectronicBilling.id -> {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
            } else {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    startViewTutorial()
                } else {
                    tutorialViewModel.setTutorial(TutorialScreenEnum.TRANSACT, false)
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                    )
                }
            }
        } else {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
            )
        }
    }

    private fun initClickListener() {
        constraintLayoutFundTransfer.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                OrganizationTransferActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        constraintLayoutBillsPayment.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                OrganizationPaymentActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        constraintLayoutCheckDeposit.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                CheckDepositActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        constraintLayoutBranchVisit.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                BranchVisitActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
        constraintLayoutElectronicBilling.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                EBillingFormActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }

        constraintLayoutRequestPayment.setOnClickListener{

            eventBus.transactSyncEvent.emmit(
                BaseEvent(TransactSyncEvent.ACTION_VALIDATE_MERCHANT_EXIST)
            )

        }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsHasPermission -> {
                    val cl = when (it.permissionCode) {
                        TransactScreenEnum.FUND_TRANSFER.name -> {
                            constraintLayoutFundTransfer
                        }
                        TransactScreenEnum.BILLS_PAYMENT.name -> {
                            constraintLayoutBillsPayment
                        }
                        else -> {
                            constraintLayoutCheckDeposit
                        }
                    }
                    cl.setEnableView(it.hasPermission)
                }
                is ShowSettingsError -> {
                    if (it.throwable is JsonParseException) {
                        setCheckDepositClickListener()
                    } else {
                        handleOnError(it.throwable)
                    }
                }
            }
        })
        viewModel.featureToggle.observe(this, Observer {
            if (it.first == FeaturesEnum.MOBILE_CHECK_DEPOSIT_VIEW) {
                if (it.second) {
                    setCheckDepositClickListener()
                    viewModel.hasPermissionChannel(
                        PermissionNameEnum.CHECK_DEPOSIT.value,
                        Constant.Permissions.CODE_RCD_MOBILE_CHECK
                    )
                } else {
                    constraintLayoutCheckDeposit.setEnableView(it.second)
                    constraintLayoutCheckDeposit.isEnabled = true
                    constraintLayoutCheckDeposit.isClickable = true
                    constraintLayoutCheckDeposit.setOnClickListener {
                        showBottomSheetError(
                            formatString(R.string.title_check_deposit_unavailable),
                            formatString(R.string.msg_check_deposit_unavailable)
                        )
                    }
                }
            } else if (it.first == FeaturesEnum.E_BILLING) {
                if (it.second) {
                    initEBillingClickEvent()
                } else {
                    constraintLayoutElectronicBilling.setEnableView(it.second)
                    constraintLayoutElectronicBilling.isEnabled = true
                    constraintLayoutElectronicBilling.isClickable = true
                    constraintLayoutElectronicBilling.setOnClickListener {
                        showBottomSheetError(
                            formatString(R.string.title_e_billing_unavailable),
                            formatString(R.string.msg_e_billing_unavailable)
                        )
                    }
                }
            }
        })
        eventBus.settingsSyncEvent.emmit(
            BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
        )
        tutorialViewModel.hasTutorial(TutorialScreenEnum.TRANSACT)


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

    private fun setCheckDepositClickListener() {
        constraintLayoutCheckDeposit.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                CheckDepositActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
            )
        }
    }

    private fun initEBillingClickEvent() {
        constraintLayoutElectronicBilling.setOnClickListener {
            navigator.navigate(
                (activity as DashboardActivity),
                EBillingFormActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
            )
        }
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_PUSH_TUTORIAL_TRANSACT -> {
                    isClickedHelpTutorial = true
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                    )
                    startTransactTutorial()
                }
            }
        }.addTo(disposables)
    }

    private fun startTransactTutorial() {
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            R.drawable.ic_tutorial_transact_orange,
            getString(R.string.title_tab_transact),
            getString(R.string.msg_tutorial_transact)
        )
    }

    private fun startViewTutorial() {
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            constraintLayoutFundTransfer,
            R.layout.frame_tutorial_upper_left,
            0f,
            false,
            getString(R.string.msg_tutorial_transact_fund_transfer),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
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
        const val FRAGMENT_REQUEST_PAYMENT = "request_payment"
    }
}
