package com.unionbankph.corporate.app.dashboard

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.jakewharton.rxbinding2.view.RxView
import com.mtramin.rxfingerprint.RxFingerprint
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_list.AccountFragment
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.*
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.fragment.DashboardFragment
import com.unionbankph.corporate.approval.presentation.ApprovalFragment
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.common.data.model.BadgeCount
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.corporate.presentation.organization.OrganizationActivity
import com.unionbankph.corporate.databinding.ActivityDashboardBinding
import com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_splash.InstapayQrSplashActivity
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogTabFragment
import com.unionbankph.corporate.payment_link.presentation.create_merchant.MerchantApplicationReceivedActivity
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.presentation.SettingsFragment
import com.unionbankph.corporate.settings.presentation.fingerprint.FingerprintBottomSheet
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.payment_link_list.PaymentLinkListFragment
import com.unionbankph.corporate.settings.presentation.splash.SplashStartedScreenActivity.Companion.existingUser
import com.unionbankph.corporate.transact.presentation.transact.TransactFragment
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.collections.set

/**
 * Created by Herald Santos
 */
class DashboardActivity : BaseActivity<ActivityDashboardBinding, DashboardViewModel>(),
    AHBottomNavigation.OnTabSelectedListener,
    OnTutorialListener,
    OnConfirmationPageCallBack,
    FingerprintBottomSheet.OnFingerPrintListener {

    private val headerDashboard by lazyFast {
        resources.getStringArray(R.array.array_dashboard_header).toMutableList()
    }

    var isOnTrialMode = false

    private lateinit var ahNotificationBuilder: AHNotification.Builder

    private var organizationBadgeCount: BadgeCount? = null

    private var alertBadgeCount: BadgeCount? = null

    private var bottomNavigationItems: HashMap<String, Int> = hashMapOf()

    private var adapter: ViewPagerAdapter? = null

    private var newUserDetectedBottomSheet: ConfirmationBottomSheet? = null

    private var trustedDeviceBottomSheet: ConfirmationBottomSheet? = null

    private var trustedDeviceResultBottomSheet: ConfirmationBottomSheet? = null

    private var fingerprintBottomSheet: ConfirmationBottomSheet? = null

    private var logoutBottomSheet: ConfirmationBottomSheet? = null

    private var role: Role? = null

    private var stackTitle: String = ""

    private var stackFlagNotification: Boolean = true

    private var stackFlagSettings: Boolean = true

    private var allowMultipleSelectionApprovals: Boolean = false

    private var hasNotificationLogs: Boolean = false

    private var badgeCount: Int = 0

    private var isBackButtonFragmentSettings: Boolean = false

    private var isBackButtonFragmentAlerts: Boolean = false

    private var isBackButtonPaymentList: Boolean = false

    private lateinit var transactFragment: TransactFragment

    private lateinit var dashboardFragment: DashboardFragment

    private var isBackButtonFragmentAccount: Boolean = false

    override fun onViewsBound() {
        super.onViewsBound()
        initViewPager()
        initBottomNavigation()

        isOnTrialMode = sharedPreferenceUtil.isTrialMode().get()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.dashBoardState.observe(this, Observer {
            when (it) {

                is ShowMerchantStatusPendingScreen -> {
                    showMerchantStatusPendingScreen()
                }
                is ShowFeatureUnavailable -> {
                    showFeatureUnavailable()
                }
                is ShowProgressLoading -> {
                    showProgressAlertDialog(DashboardActivity::class.java.simpleName)
                }
                is ShowDismissProgressLoading -> {
                    dismissProgressAlertDialog()
                }
                is NotificationSuccess -> {
                    initNotificationBadgeApprovals(it)
                }
                is ShowNotificationLogBadgeCount -> {
                    initNotificationBadgeAlerts(it)
                }
                is ShowBiometricDialog -> {
                    if (it.hasNotBiometric) {
                        showFingerprintBottomSheet()
                    } else {
                        viewModel.isReadMCDTerms()
                    }
                }
                is ShowTutorialIntroduction -> {
                    if (it.hasTutorial) {
                        startIntroductionTutorial()
                    }
                }
                is ShowNewDeviceDetected -> {
                    if (it.isNewDeviceDetected) {
                        showNewUserDetectedBottomSheet()
                    } else {
                        viewModel.isTrustedDevice()
                    }
                }
                is ShowTrustedDevice -> {
                    if (it.hasNotTrustedDevice &&
                        !intent.getBooleanExtra(EXTRA_SWITCH_ORG, false)
                    ) {
                        showTrustedDeviceBottomSheet()
                    } else {
                        viewModel.hasFingerPrint()
                    }
                }
                is ShowReadMCDTerms -> {
                    if (!it.isReadMCDTerms) {
                        showReadMCDTerms()
                    } else {
                        viewModel.hasTutorialIntroduction()
                    }
                }
                is ShowBiometricSuccess -> {
                    initEncryptedBiometric(it.token)
                }
                is ShowBiometricSetToken -> {
                    fingerprintBottomSheet?.dismiss()
                    emmitFingerPrintEventBus(true)
                    viewModel.considerAsRecentUser(PromptTypeEnum.BIOMETRIC)
                    viewModel.hasTutorialIntroduction()
                }
                is ShowTOTPSubscription -> {
                    trustedDeviceBottomSheet?.dismiss()
                    viewModel.considerAsRecentUser(PromptTypeEnum.TRUST_DEVICE)
                    showTrustDeviceResultBottomSheet()
                }
                is Success -> {
                    initDashboardViews(it.role)
                }
                is ShowSuccessBiometric -> {
                    fingerprintBottomSheet?.dismiss()
                }
                is ShowBiometricFailed -> {
                    emmitFingerPrintEventBus(false)
                    handleOnError(it.throwable)
                }
                is ShowPaymentLinkOnBoarding -> {
                    navigatePaymentLinkOnBoarding(it.merchantExists, it.fromWhatTab)
                }

                is Error -> {
                    handleOnError(it.throwable)
                }

                is ShowQrScan -> {
                    showInstapayQrScan()
                }
            }
        })

        viewModel.authState.observe(this, EventObserver {
            if (it is String) {
                logoutUser()
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initDataBus()
        tutorialEngineUtil.setOnTutorialListener(this)
        RxView.clicks(binding.viewToolbar.btnLogout)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                showLogoutBottomSheet()
            }.addTo(disposables)
        binding.viewToolbar.btnHelp.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            when (binding.bottomNavigationBTR.currentItem) {
                bottomNavigationItems[FRAGMENT_DASHBOARD] -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_ACCOUNT)
                    )
                }
                bottomNavigationItems[FRAGMENT_TRANSACT] -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_TRANSACT)
                    )
                }
                bottomNavigationItems[FRAGMENT_APPROVALS] -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_APPROVAL)
                    )
                }
                bottomNavigationItems[FRAGMENT_NOTIFICATIONS] -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_NOTIFICATIONS)
                    )
                }
                bottomNavigationItems[FRAGMENT_SETTINGS] -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_SETTINGS)
                    )
                }
            }
        }

        binding.viewToolbar.btnRequestPayment.setOnClickListener{
            viewModel.validateMerchant(DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)
        }

        RxView.clicks(binding.viewToolbar.viewBadge.viewBadgeLayout)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateOrganizationScreen()
            }.addTo(disposables)

        binding.viewToolbar.btnScan.setOnClickListener {
            viewModel.scanQR()
        }
    }

    private fun initDataBus() {
        dataBus.approvalBatchIdDataBus.flowable.subscribe {
            val badgeCountSharedApprovalsPref = sharedPreferenceUtil.badgeCountApprovalsSharedPref()
            badgeCountSharedApprovalsPref.set(
                badgeCountSharedApprovalsPref.get().minus(it.batchIds.size)
            )
            generalViewModel.updateShortCutBadgeCount()
            setupBadge(badgeCount - it.batchIds.size, false, FRAGMENT_APPROVALS)
        }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_TUTORIAL_ACCOUNT_TAP -> {
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewToolbar.viewBadge.viewBadgeLayout,
                        R.layout.frame_tutorial_upper_left,
                        getCircleFloatSize(binding.viewToolbar.viewBadge.viewBadgeLayout) -
                                resources.getDimension(R.dimen.content_spacing_half),
                        true,
                        getString(R.string.msg_tutorial_badge),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                SettingsSyncEvent.ACTION_TUTORIAL_SETTINGS_DONE -> {
                    startTutorialLogout()
                }
                SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM -> {
                    enableTabs(true)
                }
                SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM -> {
                    enableTabs(false)
                }
            }
        }.addTo(disposables)

        eventBus.notificationSyncEvent.flowable.subscribe {
            when (it.eventType) {
                NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_APPROVAL -> {
                    viewModel.getOrganizationNotification(role?.organizationId!!)
                }
                NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_ALERTS -> {
                    viewModel.getNotificationLogBadgeCount()
                }
            }
        }.addTo(disposables)

        eventBus.actionSyncEvent.flowable.subscribe(
            { eventBus ->
                when (eventBus.eventType) {
                    ActionSyncEvent.ACTION_UPDATE_NOTIFICATION_LOG_ITEM -> {
                        alertBadgeCount?.let {
                            if (it.badge != 0) {
                                val badgeCountAlertsSharedPref =
                                    sharedPreferenceUtil.badgeCountAlertsSharedPref()
                                val newBadgeCount = it.badge.minus(1)
                                badgeCountAlertsSharedPref.set(newBadgeCount)
                                generalViewModel.updateShortCutBadgeCount()
                                it.badge = newBadgeCount
                                setupBadge(it.badge, it.isColored, FRAGMENT_NOTIFICATIONS)
                            }
                        }
                    }
                    ActionSyncEvent.ACTION_NAVIGATE_ALERTS_TAB -> {
                        binding.bottomNavigationBTR.currentItem =
                            bottomNavigationItems[FRAGMENT_NOTIFICATIONS] ?: 3
                        Handler().postDelayed(
                            {
                                this.eventBus.actionSyncEvent.emmit(
                                    BaseEvent(
                                        ActionSyncEvent.ACTION_NAVIGATE_NOTIFICATION_LOG_DETAIL,
                                        eventBus.payload.notNullable()
                                    )
                                )
                            }, resources.getInteger(R.integer.anim_duration_normal).toLong()
                        )
                    }
                    ActionSyncEvent.ACTION_UPDATE_MARK_ALL_AS_READ_ICON -> {
                        hasNotificationLogs = eventBus.payload == "1"
                    }
                    ActionSyncEvent.ACTION_AGREE_PRIVACY_POLICY -> {
                        viewModel.hasTutorialIntroduction()
                    }
                }
            }
        ) {
            Timber.e(it, "actionSyncEvent")
        }.addTo(disposables)

        eventBus.transactSyncEvent.flowable.subscribe({
            when (it.eventType) {
                TransactSyncEvent.ACTION_VALIDATE_MERCHANT_EXIST -> {
                    viewModel.validateMerchant(DashboardViewModel.FROM_TRANSACT_TAB)
                }
                TransactSyncEvent.ACTION_GO_TO_PAYMENT_LINK_LIST -> {
                    isBackButtonPaymentList = true

                    setToolbarTitle(
                        getString(R.string.title_payment_links),
                        hasBackButton = true,
                        hasMenuItem = false
                    )
                    binding.viewToolbar.btnHelp.visibility(false)
                    binding.viewToolbar.btnScan.visibility(false)
                    runPostDelayed({
                        transactFragment.navigateToPaymentLinkFragment()
                    }, 100)
                }
            }
        }) {
            Timber.e(it, "transactSyncEvent error: ${it.message}")
        }.addTo(disposables)

        eventBus.transactSyncEvent.flowable.subscribe({
            when (it.eventType) {
                TransactSyncEvent.ACTION_VALIDATE_MERCHANT_EXIST_DASHBOARD -> {
                    viewModel.validateMerchant(DashboardViewModel.FROM_DASHBOARD_TAB)
                }
                TransactSyncEvent.ACTION_GO_TO_PAYMENT_LINK_LIST_DASHBOARD -> {

                    setToolbarTitle(
                        getString(R.string.title_payment_links),
                        hasBackButton = true,
                        hasMenuItem = false
                    )
                    binding.viewToolbar.btnHelp.visibility(false)
                    binding.viewToolbar.btnScan.visibility(false)
                    isBackButtonFragmentAccount = true
                    runPostDelayed({
                        dashboardFragment.navigateToPaymentLinkFragment()
                    }, 100)
                }
            }
        }) {
            Timber.e(it, "transactSyncEvent error: ${it.message}")
        }.addTo(disposables)

        eventBus.actionSyncEvent.flowable.subscribe({
            when(it.eventType) {
                ActionSyncEvent.ACTION_VIEW_ALL_ACCOUNTS -> {
                    setToolbarTitle(
                        getString(R.string.title_dashboard_header_accounts),
                        hasBackButton = true,
                        hasMenuItem = false
                    )
                    binding.viewToolbar.btnRequestPayment.visibility(false)
                    binding.viewToolbar.btnHelp.visibility(false)
                    binding.viewToolbar.btnScan.visibility(false)
                    isBackButtonFragmentAccount = true
                }

            }
        }){
            Timber.e(it, "actionSyncEvent error:  ${it.message}")
        }.addTo(disposables)
    }

    private fun initNotificationBadgeAlerts(it: ShowNotificationLogBadgeCount) {
        alertBadgeCount = it.badgeCount
        val badgeCountSharedAlertsPref = sharedPreferenceUtil.badgeCountAlertsSharedPref()
        badgeCountSharedAlertsPref.set(it.badgeCount.badge)
        generalViewModel.updateShortCutBadgeCount()
        setupBadge(
            it.badgeCount.badge,
            it.badgeCount.isColored,
            FRAGMENT_NOTIFICATIONS
        )
    }

    private fun initNotificationBadgeApprovals(it: NotificationSuccess) {
        organizationBadgeCount = it.profileBadgeCount
        val badgeCountSharedApprovalsPref = sharedPreferenceUtil.badgeCountApprovalsSharedPref()
        badgeCountSharedApprovalsPref.set(
            it.approvalBadgeCount.badge.plus(it.profileBadgeCount.badge)
        )
        generalViewModel.updateShortCutBadgeCount()
        setupBadge(
            it.approvalBadgeCount.badge,
            it.approvalBadgeCount.isColored,
            FRAGMENT_APPROVALS
        )
        setOrganizationBadge(it.profileBadgeCount)
    }

    private fun initEncryptedBiometric(token: String) {
        if (RxFingerprint.isAvailable(this)) {
            val fingerprintBottomSheet = FingerprintBottomSheet.newInstance(
                token,
                FingerprintBottomSheet.ENCRYPT_TYPE
            )
            fingerprintBottomSheet.setOnFingerPrintListener(this)
            fingerprintBottomSheet.encrypt(
                this,
                FingerprintBottomSheet.EXTRA_TOKEN,
                token
            )
        } else if (BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                .BIOMETRIC_SUCCESS
        ) {
            viewModel.setTokenFingerPrint(token)
        }


    }

    private fun initDashboardViews(role: Role) {
        removeElevation(binding.viewToolbar.appBarLayout)

        this.role = role
        role.let {
            binding.viewToolbar.textViewCorporationName.text = it.organizationName
            binding.viewToolbar.viewBadge.textViewInitial.text =
                viewUtil.getCorporateOrganizationInitial(it.organizationName)
            binding.viewToolbar.textViewTitle.text =
                when (isOnTrialMode) {
                true -> it.organizationName
                else -> getString(R.string.title_dashboard_header_dashboard)
            }
            if (!it.hasApproval && !it.isApprover) {
                binding.bottomNavigationBTR.disableItemAtPosition(2)
            }
        }
        viewModel.isNewUserDetected()
        viewModel.getOrganizationNotification(role.organizationId.notNullable())
        viewModel.getNotificationLogBadgeCount()
        Handler().postDelayed(
            {
                initPushNotificationLandingPage()
            }, resources.getInteger(R.integer.time_delay_load_transaction_detail).toLong()
        )
    }

    private fun popStackFragmentDashboard(){
        isBackButtonFragmentAccount = false
        setToolbarTitle(
            getString(R.string.title_dashboard_header_dashboard),
            hasBackButton = false,
            hasMenuItem = true
        )
        val dashboardFragment =
            adapter?.getItem(bottomNavigationItems[FRAGMENT_DASHBOARD] ?:0)!!
        dashboardFragment.childFragmentManager.popBackStackImmediate()
        binding.viewToolbar.btnRequestPayment.visibility(true)
        binding.viewToolbar.btnHelp.visibility(true)
        binding.viewToolbar.btnScan.visibility(true)
    }

    fun popStackFragmentSettings() {
        val settingsFragment =
            adapter?.getItem(bottomNavigationItems[FRAGMENT_SETTINGS] ?: 4)!!
        settingsFragment.childFragmentManager.popBackStackImmediate()
        val fragmentManager = settingsFragment.childFragmentManager
        val fragmentTag = settingsFragment
            .childFragmentManager
            .getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
            .name
        when (fragmentTag) {
            SettingsFragment.FRAGMENT_PROFILE -> {
                setToolbarTitle(
                    getString(R.string.title_profile),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            SettingsFragment.FRAGMENT_SECURITY -> {
                setToolbarTitle(
                    getString(R.string.title_security),
                    hasBackButton = true,
                    hasMenuItem = true
                )
            }
            SettingsFragment.FRAGMENT_NOTIFICATION -> {
                setToolbarTitle(
                    getString(R.string.title_notifications),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            SettingsFragment.FRAGMENT_NOTIFICATION_DETAIL -> {
                setToolbarTitle(
                    getString(R.string.title_notifications),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            SettingsFragment.FRAGMENT_SECURITY_MANAGE_OTP -> {
                setToolbarTitle(
                    getString(R.string.title_otp),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            SettingsFragment.FRAGMENT_SECURITY_ENABLE_OTP -> {
                setToolbarTitle(
                    getString(R.string.title_enable_otp),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            SettingsFragment.FRAGMENT_SECURITY_RECEIVE_OTP -> {
                setToolbarTitle(
                    getString(R.string.title_receive_otp),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            SettingsFragment.FRAGMENT_MANAGE_DEVICES,
            SettingsFragment.FRAGMENT_MANAGE_DEVICE_DETAIL -> {
                setToolbarTitle(
                    getString(R.string.title_manage_devices),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            else -> {
                setToolbarTitle(
                    getString(R.string.title_dashboard_header_settings),
                    hasBackButton = false,
                    hasMenuItem = true
                )
            }
        }
    }

    private fun popStackFragmentNotifications() {
        val notificationTabFragment =
            adapter?.getItem(bottomNavigationItems[FRAGMENT_NOTIFICATIONS] ?: 3)!!
        notificationTabFragment.childFragmentManager.popBackStackImmediate()
        val fragmentManager = notificationTabFragment.childFragmentManager
        val fragmentTag = notificationTabFragment
            .childFragmentManager
            .getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
            .name
        when (fragmentTag) {
            NotificationLogTabFragment.FRAGMENT_NOTIFICATION_LOG_DETAIL -> {
                binding.viewToolbar.btnMarkAll.visibility(false)
                setToolbarTitle(
                    getString(
                        R.string.title_dashboard_header_notifications
                    ),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            else -> {
                binding.viewToolbar.btnMarkAll.visibility(hasNotificationLogs)
                setToolbarTitle(
                    getString(
                        R.string.title_dashboard_header_notifications
                    ),
                    hasBackButton = false,
                    hasMenuItem = true
                )
            }
        }
    }


    private fun popBackStackTransact() {
        val transactTabFragment = adapter?.getItem(bottomNavigationItems[FRAGMENT_TRANSACT] ?: 1)!!
        val fragmentManager = transactTabFragment.childFragmentManager
        val fragmentTag = transactTabFragment
            .childFragmentManager
            .getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
            .name
        if (fragmentTag.equals(TransactFragment.FRAGMENT_REQUEST_PAYMENT, true)) {
            transactTabFragment.childFragmentManager.popBackStackImmediate()
            isBackButtonPaymentList = false
            setToolbarTitle(
                getString(R.string.title_dashboard_header_transact),
                hasBackButton = false,
                hasMenuItem = true
            )
            binding.viewToolbar.btnHelp.visibility(true)
            binding.viewToolbar.btnScan.visibility(true)
        }
    }

    override fun onBackPressed() {
        if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_DASHBOARD]) {
            val dashboardFragment =
                adapter?.getItem(bottomNavigationItems[FRAGMENT_DASHBOARD] ?: 0)!!
                if(dashboardFragment.isAdded){
                    val count = dashboardFragment.childFragmentManager.backStackEntryCount
                    if (count == 0){
                        showLogoutBottomSheet()
                    }else{
                        if(isBackButtonFragmentAccount){
                            popStackFragmentDashboard()
                        }
                    }
                }
        }else if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) {
            val settingsFragment =
                adapter?.getItem(bottomNavigationItems[FRAGMENT_SETTINGS]!!)!!
            if (settingsFragment.isAdded) {
                val count = settingsFragment.childFragmentManager.backStackEntryCount
                if (count == 1 ||
                    binding.viewPagerBTR.currentItem != bottomNavigationItems[FRAGMENT_SETTINGS]
                ) {
                    showLogoutBottomSheet()
                } else {
                    popStackFragmentSettings()
                }
            }
        } else if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) {
            val notificationTabFragment =
                adapter?.getItem(bottomNavigationItems[FRAGMENT_NOTIFICATIONS] ?: 3)!!
            if (notificationTabFragment.isAdded) {
                val count = notificationTabFragment.childFragmentManager.backStackEntryCount
                if (count == 1 ||
                    binding.viewPagerBTR.currentItem != bottomNavigationItems[FRAGMENT_NOTIFICATIONS]
                ) {
                    showLogoutBottomSheet()
                } else {
                    popStackFragmentNotifications()
                }
            }
        } else if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_TRANSACT]) {
            val transactTabFragment =
                adapter?.getItem(bottomNavigationItems[FRAGMENT_TRANSACT] ?: 1)!!
            if (transactTabFragment.isAdded) {

                val count = transactTabFragment.childFragmentManager.backStackEntryCount
                if (count == 0 || binding.viewPagerBTR.currentItem != bottomNavigationItems[FRAGMENT_TRANSACT]) {
                    showLogoutBottomSheet()
                } else {
                    popBackStackTransact()
                }
            }
        } else {
            if (binding.viewApprovalsNavigation.viewApprovalsNavigationLayout.visibility == View.VISIBLE) {
                eventBus.actionSyncEvent.emmit(
                    BaseEvent(ActionSyncEvent.ACTION_CANCEL_MULTIPLE_SELECTION_APPROVAL)
                )
            } else {
                showLogoutBottomSheet()
            }
        }
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        if (!wasSelected) {
            viewModel.getOrganizationNotification(role?.organizationId.notNullable())
            if ((isBackButtonFragmentSettings &&
                        position == bottomNavigationItems[FRAGMENT_SETTINGS]) ||
                (isBackButtonFragmentAlerts &&
                        position == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) ||
                isBackButtonPaymentList && position == bottomNavigationItems[FRAGMENT_TRANSACT]
            ) {
                binding.viewToolbar.viewBadgeCount.widgetBadgeNormalLayout.visibility(false)
                binding.viewToolbar.btnLogout.visibility(false)
                binding.viewToolbar.btnMarkAll.visibility(false)
                binding.viewToolbar.btnRequestPayment.visibility(
                    when (binding.viewPagerBTR.currentItem) {
                        bottomNavigationItems[FRAGMENT_TRANSACT] -> true
                        else -> false
                    }
                )
                binding.viewToolbar.viewBadge.imageViewInitial.setImageResource(R.drawable.ic_msme_back_button_orange)
                //if (isSME) binding.viewToolbar.viewBadge.imageViewInitial.setColor(R.color.colorInfo)
                binding.viewToolbar.viewBadge.textViewInitial.visibility = View.GONE
                binding.viewToolbar.viewBadge.viewBadgeLayout.setOnClickListener {
                    when (binding.viewPagerBTR.currentItem) {
                        bottomNavigationItems[FRAGMENT_SETTINGS] -> popStackFragmentSettings()
                        bottomNavigationItems[FRAGMENT_NOTIFICATIONS] -> popStackFragmentNotifications()
                        bottomNavigationItems[FRAGMENT_TRANSACT] -> popBackStackTransact()
                    }
                }
                binding.viewToolbar.textViewTitle.text = stackTitle
            } else {
                if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS] &&
                    binding.viewToolbar.viewBadge.textViewInitial.visibility != View.VISIBLE
                ) {
                    isBackButtonFragmentSettings = true
                } else if (
                    binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS] &&
                    binding.viewToolbar.viewBadge.textViewInitial.visibility != View.VISIBLE
                ) {
                    isBackButtonFragmentAlerts = true
                }
                binding.viewToolbar.btnMarkAll.visibility(
                    position == bottomNavigationItems[FRAGMENT_NOTIFICATIONS] && hasNotificationLogs
                )
                binding.viewToolbar.btnScan.visibility(position == bottomNavigationItems[FRAGMENT_DASHBOARD] ||
                        position == bottomNavigationItems[FRAGMENT_TRANSACT])
                binding.viewToolbar.viewBadge.imageViewInitial.setImageResource(R.drawable.circle_medium_gradient_orange)
                if (isSME) binding.viewToolbar.viewBadge.imageViewInitial.clearTheme()
                binding.viewToolbar.viewBadge.textViewInitial.visibility = View.VISIBLE
                setOrganizationBadge(organizationBadgeCount)
                binding.viewToolbar.viewBadge.viewBadgeLayout.setOnClickListener { navigateOrganizationScreen() }
                binding.viewToolbar.textViewTitle.text = headerDashboard[position]
            }
//            if (isSME) {
//                if (position == bottomNavigationItems[FRAGMENT_APPROVALS]) {
//                    removeElevation(binding.viewToolbar.appBarLayout)
//                } else {
//                    addElevation(binding.viewToolbar.appBarLayout)
//                }
//            }
            binding.viewApprovalsNavigation.viewApprovalsNavigationLayout.visibility(
                position == bottomNavigationItems[FRAGMENT_APPROVALS] &&
                        allowMultipleSelectionApprovals
            )
            binding.viewToolbar.btnEditApproval.visibility(position == bottomNavigationItems[FRAGMENT_APPROVALS])
            binding.viewToolbar.btnEditApproval.setEnableView(allowMultipleSelectionApprovals)
            binding.viewToolbar.btnHelp.visibility(
                position == bottomNavigationItems[FRAGMENT_DASHBOARD] ||
                        position == bottomNavigationItems[FRAGMENT_TRANSACT] ||
                        position == bottomNavigationItems[FRAGMENT_APPROVALS] ||
                        (position == bottomNavigationItems[FRAGMENT_NOTIFICATIONS] &&
                                stackFlagNotification) ||
                        (position == bottomNavigationItems[FRAGMENT_SETTINGS] &&
                                stackFlagSettings)
            )
            binding.viewToolbar.btnRequestPayment.visibility(
                position == bottomNavigationItems[FRAGMENT_DASHBOARD] ||
                        ((position == bottomNavigationItems[FRAGMENT_TRANSACT]))
            )
            if (position == bottomNavigationItems[FRAGMENT_SETTINGS]) {
                val settingsFragment =
                    adapter?.getItem(bottomNavigationItems[FRAGMENT_SETTINGS]!!)!!
                if (settingsFragment.isAdded) {
                    val count = settingsFragment.childFragmentManager.backStackEntryCount
                    binding.viewToolbar.btnLogout.visibility(count == 1)
                }
            } else {
                binding.viewToolbar.btnLogout.visibility(false)
            }
            initForceTutorialTabs(position)
        } else {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_SCROLL_TO_TOP)
            )
        }
        if(binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_DASHBOARD] && isBackButtonFragmentAccount){
            popStackFragmentDashboard()
        }
        if(isBackButtonPaymentList && position == bottomNavigationItems[FRAGMENT_TRANSACT]){
            popBackStackTransact()
        }

        binding.viewPagerBTR.currentItem = position
        return true
    }

    override fun onClickSkipButtonTutorial(spotlight: Spotlight) {
        isSkipTutorial = true
        viewModel.skipTutorial()
        spotlight.closeSpotlight()
    }

    override fun onClickOkButtonTutorial(spotlight: Spotlight) {
        spotlight.closeCurrentTarget()
    }

    override fun onStartedTutorial(view: View?, viewTarget: View) {
        if (view != null) {
            when (view) {
                binding.viewToolbar.btnHelp,
                binding.viewToolbar.btnLogout,
                binding.viewToolbar.viewBadge.viewBadgeLayout -> {
                    val constraintSet = ConstraintSet()
                    val constraintLayoutTutorial =
                        viewTarget.findViewById<ConstraintLayout>(
                            R.id.constraintLayoutTutorial
                        )
                    val imgArrow = viewTarget.findViewById<ImageView>(R.id.imageViewArrow)
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)
                    var paramsSource: ViewGroup.LayoutParams? = imgArrow.layoutParams
                    if (paramsSource == null) paramsSource = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val widthSpecSource = viewUtil.makeMeasureSpec(paramsSource.width)
                    val heightSpecSource = viewUtil.makeMeasureSpec(paramsSource.height)
                    imgArrow.measure(widthSpecSource, heightSpecSource)
                    val marginTop = rect.bottom + resources.getDimension(
                        R.dimen.content_group_spacing
                    ).toInt()
                    val marginLeft = if (view == binding.viewToolbar.viewBadge.viewBadgeLayout) {
                        rect.right - ((rect.right - rect.left) / 2)
                    } else {
                        rect.right - ((rect.right - rect.left) / 2) -
                                imgArrow.measuredWidth - ((rect.right - rect.left) / 2)
                    }
                    constraintSet.clone(constraintLayoutTutorial)
                    constraintSet.connect(
                        imgArrow.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP, marginTop
                    )
                    constraintSet.connect(
                        imgArrow.id, ConstraintSet.START, ConstraintSet.PARENT_ID,
                        ConstraintSet.START, marginLeft
                    )
                    constraintSet.applyTo(constraintLayoutTutorial)
                }
            }
        }
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            eventBus.settingsSyncEvent.emmit(BaseEvent(SettingsSyncEvent.ACTION_SKIP_TUTORIAL))
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
            )
        } else {
            if (view != null) {
                when (view) {
                    binding.viewToolbar.btnHelp -> {
                        viewModel.setTutorialIntroduction(false)
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                    binding.viewToolbar.viewBadge.viewBadgeLayout -> {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_BOTTOM)
                        )
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                    binding.viewToolbar.btnLogout -> {
                        viewModel.setTutorialUser(false)
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
            } else {
                tutorialEngineUtil.startTutorial(
                    this,
                    binding.viewToolbar.btnHelp,
                    R.layout.frame_tutorial_upper_right,
                    getCircleFloatSize(binding.viewToolbar.btnHelp),
                    true,
                    getString(R.string.msg_tutorial_help),
                    GravityEnum.BOTTOM,
                    OverlayAnimationEnum.ANIM_EXPLODE
                )
            }
        }
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        when (tag) {
            TAG_NEW_USER_DETECTED_DIALOG -> {
                newUserDetectedBottomSheet?.dismiss()
                viewModel.setNewUserDetected(false)
                viewModel.isTrustedDevice()
            }
            TAG_TRUSTED_DEVICE_DIALOG -> {
                trustedDeviceBottomSheet?.dismiss()
                viewModel.considerAsRecentUser(PromptTypeEnum.TRUST_DEVICE)
                viewModel.hasFingerPrint()
            }
            TAG_FINGERPRINT_DIALOG -> {
                fingerprintBottomSheet?.dismiss()
                emmitFingerPrintEventBus(false)
                viewModel.considerAsRecentUser(PromptTypeEnum.BIOMETRIC)
                viewModel.hasTutorialIntroduction()
            }
            TAG_TRUSTED_DEVICE_RESULT_DIALOG -> {
                trustedDeviceResultBottomSheet?.dismiss()
                viewModel.hasFingerPrint()
            }
            TAG_LOGOUT_DIALOG -> {
                logoutBottomSheet?.dismiss()
            }
        }
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        when (tag) {
            TAG_TRUSTED_DEVICE_DIALOG -> {
                viewModel.totpSubscribe(ManageDeviceForm(""))
            }
            TAG_FINGERPRINT_DIALOG -> {
                viewModel.setFingerPrint()
            }
            TAG_LOGOUT_DIALOG -> {
                logoutBottomSheet?.dismiss()
                viewModel.logout()
            }
        }
    }

    override fun setOnEncryptedToken(token: String) {
        viewModel.setTokenFingerPrint(token)
    }

    private fun setupBadge(badgeCount: Int = 0, isColored: Boolean, fragment: String) {
        if (fragment == FRAGMENT_APPROVALS) {
            this.badgeCount = badgeCount
        }
        setBottomNavigationBadge(
            badgeCount, bottomNavigationItems[fragment]!!,
            if (isColored) R.color.colorRedBadge else R.color.colorGrayBadge
        )
    }

    private fun initForceTutorialTabs(position: Int) {
        when (position) {
            bottomNavigationItems[FRAGMENT_DASHBOARD] -> {
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_ACCOUNT)
                )
            }
        }
    }

    private fun setBottomNavigationBadge(badgeCount: Int, position: Int, color: Int) {
        ahNotificationBuilder = AHNotification.Builder()
            .setText(if (badgeCount > 0) badgeCount.toString() else "")
            .setBackgroundColor(ContextCompat.getColor(this, color))
            .setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
        val builder = ahNotificationBuilder.build()
        binding.bottomNavigationBTR.setNotification(builder, position)
    }

    private fun setOrganizationBadge(badgeCount: BadgeCount?) {
        if (badgeCount == null) return
        if (binding.viewToolbar.viewBadgeCount.widgetBadgeNormalLayout.visibility == View.VISIBLE) {
            binding.viewToolbar.viewBadgeCount.widgetBadgeNormalLayout
                .visibility(
                    badgeCount.badge > 0 &&
                            binding.viewToolbar.viewBadge.textViewInitial.visibility == View.VISIBLE
                )
        } else {
            if (binding.viewToolbar.viewBadge.textViewInitial.visibility == View.VISIBLE && badgeCount.badge > 0)
                viewUtil.startAnimateView(
                    true,
                    binding.viewToolbar.viewBadgeCount.widgetBadgeNormalLayout,
                    R.anim.anim_popup
                )
        }
        binding.viewToolbar.viewBadgeCount.textViewBadgeCount.text = badgeCount.badge.toString()
        binding.viewToolbar.viewBadgeCount.textViewBadgeCount.setBackgroundResource(
            if (!badgeCount.isColored)
                R.drawable.circle_gray_badge
            else
                R.drawable.circle_red_badge
        )
    }

    private fun initBottomNavigation() {
        bottomNavigationItems[FRAGMENT_DASHBOARD] = 0
        bottomNavigationItems[FRAGMENT_TRANSACT] = 1
        bottomNavigationItems[FRAGMENT_APPROVALS] = 2
        bottomNavigationItems[FRAGMENT_NOTIFICATIONS] = 3
        bottomNavigationItems[FRAGMENT_SETTINGS] = 4
        val item1 =
            AHBottomNavigationItem(getString(R.string.title_tab_dashboard), R.drawable.ic_vector_dashboard_dashboard)
        val item2 = AHBottomNavigationItem(
            getString(R.string.title_tab_transact), R.drawable.ic_send_request
        )
        val item3 =
            AHBottomNavigationItem(getString(R.string.title_tab_approvals), R.drawable.ic_approval)
        val item4 = AHBottomNavigationItem(
            getString(R.string.title_tab_notifications),
            R.drawable.ic_notification
        )
        val item5 =
            AHBottomNavigationItem(getString(R.string.title_tab_settings), R.drawable.ic_settings)
        binding.bottomNavigationBTR.defaultBackgroundColor =
            ContextCompat.getColor(this, R.color.colorWhite)
        binding.bottomNavigationBTR.accentColor = getColorFromAttr(R.attr.colorAccent)
        binding.bottomNavigationBTR.inactiveColor =
            ContextCompat.getColor(this, R.color.colorTextTab)
        binding.bottomNavigationBTR.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        binding.bottomNavigationBTR.setTitleTextSize(
            resources.getDimension(R.dimen.navigation_bottom_text_size_normal),
            resources.getDimension(R.dimen.navigation_bottom_text_size_normal)
        )
        binding.bottomNavigationBTR.addItem(item1)
        binding.bottomNavigationBTR.addItem(item2)
        binding.bottomNavigationBTR.addItem(item3)
        binding.bottomNavigationBTR.addItem(item4)
        binding.bottomNavigationBTR.addItem(item5)
        binding.bottomNavigationBTR.currentItem = 0
        binding.bottomNavigationBTR.setNotificationMarginLeft(
            resources.getDimension(R.dimen.bottom_notification_margin).toInt(),
            resources.getDimension(R.dimen.bottom_notification_margin).toInt()
        )
        binding.bottomNavigationBTR.setOnTabSelectedListener(this)
        binding.bottomNavigationBTR.post {
            if (binding.bottomNavigationBTR.getViewAtPosition(0) != null &&
                binding.bottomNavigationBTR.getViewAtPosition(1) != null &&
                binding.bottomNavigationBTR.getViewAtPosition(2) != null &&
                binding.bottomNavigationBTR.getViewAtPosition(3) != null &&
                binding.bottomNavigationBTR.getViewAtPosition(4) != null
            ) {
                binding.bottomNavigationBTR.getViewAtPosition(0).id = R.id.tabAccounts
                binding.bottomNavigationBTR.getViewAtPosition(1).id = R.id.tabTransact
                binding.bottomNavigationBTR.getViewAtPosition(2).id = R.id.tabApprovals
                binding.bottomNavigationBTR.getViewAtPosition(3).id = R.id.tabProfile
                binding.bottomNavigationBTR.getViewAtPosition(4).id = R.id.tabSettings
            }
        }
    }

    private fun enableTabs(isEnable: Boolean) {
        if (binding.bottomNavigationBTR.getViewAtPosition(0) != null &&
            binding.bottomNavigationBTR.getViewAtPosition(1) != null &&
            binding.bottomNavigationBTR.getViewAtPosition(2) != null &&
            binding.bottomNavigationBTR.getViewAtPosition(3) != null &&
            binding.bottomNavigationBTR.getViewAtPosition(4) != null
        ) {
            binding.bottomNavigationBTR.getViewAtPosition(0).isEnabled = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(0).isClickable = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(1).isEnabled = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(1).isClickable = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(2).isEnabled = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(2).isClickable = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(3).isEnabled = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(3).isClickable = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(4).isEnabled = isEnable
            binding.bottomNavigationBTR.getViewAtPosition(4).isClickable = isEnable
        }
    }

    private fun initViewPager() {
        existingUser = 0
        transactFragment = TransactFragment()
        dashboardFragment = DashboardFragment()

        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter?.addFragment(dashboardFragment, FRAGMENT_DASHBOARD)
        adapter?.addFragment(transactFragment, FRAGMENT_TRANSACT)
        adapter?.addFragment(ApprovalFragment(), FRAGMENT_APPROVALS)
        adapter?.addFragment(NotificationLogTabFragment(), FRAGMENT_NOTIFICATIONS)
        adapter?.addFragment(SettingsFragment(), FRAGMENT_SETTINGS)
        // viewPagerBTR.setPageTransformer(false, FadePageTransformer())
        binding.viewPagerBTR.setPagingEnabled(false)
        binding.viewPagerBTR.offscreenPageLimit = 4
        binding.viewPagerBTR.adapter = adapter
    }

    private fun getCircleFloatSize(view: View): Float {
        return view.height.toFloat() - resources.getDimension(R.dimen.view_tutorial_radius_padding)
    }

    private fun startIntroductionTutorial() {
        eventBus.settingsSyncEvent.emmit(BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM))
        Handler().postDelayed(
            {
                tutorialEngineUtil.startTutorial(
                    this,
                    R.drawable.ic_tutorial_unionbank_orange,
                    getString(R.string.title_welcome),
                    getString(
                        if (isSME) {
                            R.string.msg_tutorial_welcome_sme
                        } else {
                            R.string.msg_tutorial_welcome
                        }
                    )
                )
            }, resources.getInteger(R.integer.time_enter_tutorial_sample_data).toLong()
        )
    }

    fun showFingerprintBottomSheet() {
        if (RxFingerprint.isAvailable(this)) {
            fingerprintBottomSheet = ConfirmationBottomSheet.newInstance(
                R.drawable.ic_fingerprint_white_48dp,
                getString(R.string.title_enable_fingerprint),
                getString(R.string.desc_fingerprint),
                getString(R.string.action_link),
                getString(R.string.action_not_now)
            )
        } else if (BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                .BIOMETRIC_SUCCESS
        ) {
            fingerprintBottomSheet = ConfirmationBottomSheet.newInstance(
                R.drawable.ic_fingerprint_white_48dp,
                getString(R.string.title_enable_face_id),
                getString(R.string.desc_face_id),
                getString(R.string.action_link),
                getString(R.string.action_not_now)
            )
        }

        fingerprintBottomSheet?.isCancelable = false
        fingerprintBottomSheet?.setOnConfirmationPageCallBack(this)
        fingerprintBottomSheet?.show(
            supportFragmentManager,
            TAG_FINGERPRINT_DIALOG
        )
    }

    private fun showTrustDeviceResultBottomSheet() {
        trustedDeviceResultBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_device_android_white,
            formatString(
                R.string.param_title_successfully_trusted_device_name,
                settingsUtil.getDeviceName()
            ),
            getString(R.string.desc_successfully_trusted_device),
            null,
            getString(R.string.action_close)
        )
        trustedDeviceResultBottomSheet?.isCancelable = false
        trustedDeviceResultBottomSheet?.setOnConfirmationPageCallBack(this)
        trustedDeviceResultBottomSheet?.show(
            supportFragmentManager,
            TAG_TRUSTED_DEVICE_RESULT_DIALOG
        )
    }

    private fun showTrustedDeviceBottomSheet() {
        trustedDeviceBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_device_android_white,
            formatString(R.string.param_title_trust_device_name, settingsUtil.getDeviceName()),
            getString(R.string.desc_dialog_trust_device),
            getString(R.string.action_trust),
            getString(R.string.action_not_now)
        )
        trustedDeviceBottomSheet?.isCancelable = false
        trustedDeviceBottomSheet?.setOnConfirmationPageCallBack(this)
        trustedDeviceBottomSheet?.show(
            supportFragmentManager,
            TAG_TRUSTED_DEVICE_DIALOG
        )
    }

    private fun showReadMCDTerms() {
        val readMCDTermsBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_important),
            getString(R.string.desc_dialog_read_mcd_terms),
            getString(R.string.action_continue)
        )
        readMCDTermsBottomSheet.isCancelable = false
        readMCDTermsBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {

            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                readMCDTermsBottomSheet.dismiss()
                navigatePrivacyPolicyScreen()
            }

        })
        readMCDTermsBottomSheet.isCancelable = false
        readMCDTermsBottomSheet.show(
            supportFragmentManager,
            TAG_READ_MCD_TERMS_DIALOG
        )
    }

    private fun showNewUserDetectedBottomSheet() {
        newUserDetectedBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_security_white,
            formatString(R.string.title_new_user_detected),
            formatString(R.string.desc_new_user_detected),
            actionNegative = formatString(R.string.action_close)
        )
        newUserDetectedBottomSheet?.isCancelable = false
        newUserDetectedBottomSheet?.setOnConfirmationPageCallBack(this)
        newUserDetectedBottomSheet?.show(
            supportFragmentManager,
            TAG_NEW_USER_DETECTED_DIALOG
        )
    }

    private fun showLogoutBottomSheet() {
        logoutBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            getString(R.string.title_sign_out),
            getString(R.string.msg_sign_out),
            getString(R.string.action_yes),
            getString(R.string.action_no)
        )
        logoutBottomSheet?.isCancelable = false
        logoutBottomSheet?.setOnConfirmationPageCallBack(this)
        logoutBottomSheet?.show(
            supportFragmentManager,
            TAG_LOGOUT_DIALOG
        )
    }

    private fun emmitFingerPrintEventBus(isEnable: Boolean) {
        eventBus.biometricSyncEvent.emmit(
            BaseEvent(
                BiometricSyncEvent.ACTION_UPDATE_BIOMETRIC,
                isEnable
            )
        )
    }

    private fun startTutorialLogout() {
        tutorialEngineUtil.startTutorial(
            this,
            binding.viewToolbar.btnLogout,
            R.layout.frame_tutorial_upper_right,
            getCircleFloatSize(binding.viewToolbar.btnLogout),
            true,
            getString(R.string.msg_tutorial_logout),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun navigatePrivacyPolicyScreen() {
        val bundle = Bundle()
        bundle.putString(
            PrivacyPolicyActivity.EXTRA_REQUEST_PAGE,
            PrivacyPolicyActivity.PAGE_MCD_TERMS
        )
        navigator.navigate(
            this,
            PrivacyPolicyActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateOrganizationScreen() {
        navigator.navigate(
            this,
            OrganizationActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun navigatePaymentLinkOnBoarding(merchantExists: Boolean, fromWhatTab: String) {

        val bundle = Bundle()
        bundle.putBoolean(
            RequestPaymentSplashActivity.EXTRA_MERCHANT_EXISTS,
            merchantExists
        )
        bundle.putString(
            RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB,
            fromWhatTab
        )

        navigator.navigate(
            this,
            RequestPaymentSplashActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showInstapayQrScan() {
        navigator.navigate(
            this,
            InstapayQrSplashActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showMerchantStatusPendingScreen(){
        navigator.navigate(
            this,
            MerchantApplicationReceivedActivity::class.java,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showFeatureUnavailable() {

        binding.errorMerchantRejected.visibility = View.VISIBLE
        binding.viewFeatureUnavailable.btnFeatureUnavailableBackToDashboard.setOnClickListener {
            binding.errorMerchantRejected.visibility = View.GONE
        }
    }


    fun setToolbarTitle(title: String, hasBackButton: Boolean, hasMenuItem: Boolean = false) {
        binding.viewToolbar.textViewTitle.text = title
        binding.viewToolbar.btnHelp.visibility(hasMenuItem)
        if (!hasBackButton) {
            binding.viewToolbar.btnLogout.visibility(
                binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]
            )
            if(binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) binding.viewToolbar.btnScan.visibility(false)
            binding.viewToolbar.viewBadge.imageViewInitial.setImageResource(R.drawable.circle_medium_gradient_orange)
            if (isSME) binding.viewToolbar.viewBadge.imageViewInitial.clearTheme()
            binding.viewToolbar.viewBadge.viewBadgeLayout.setOnClickListener { navigateOrganizationScreen() }
            binding.viewToolbar.viewBadge.textViewInitial.visibility = View.VISIBLE
            setOrganizationBadge(organizationBadgeCount)
        } else {
            binding.viewToolbar.viewBadgeCount.widgetBadgeNormalLayout.visibility(false)
            binding.viewToolbar.btnLogout.visibility(false)
            binding.viewToolbar.viewBadge.imageViewInitial.setImageResource(R.drawable.ic_msme_back_button_orange)
            //if (isSME) binding.viewToolbar.viewBadge.imageViewInitial.setColor(R.color.colorInfo)
            binding.viewToolbar.viewBadge.viewBadgeLayout.setOnClickListener {
                when (binding.viewPagerBTR.currentItem) {
                    bottomNavigationItems[FRAGMENT_SETTINGS] -> popStackFragmentSettings()
                    bottomNavigationItems[FRAGMENT_NOTIFICATIONS] -> popStackFragmentNotifications()
                    bottomNavigationItems[FRAGMENT_TRANSACT] -> popBackStackTransact()
                    bottomNavigationItems[FRAGMENT_DASHBOARD] -> popStackFragmentDashboard()
                }
            }
            binding.viewToolbar.viewBadge.textViewInitial.visibility = View.GONE
        }
        if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) {
            isBackButtonFragmentSettings = hasBackButton
            stackFlagSettings = hasMenuItem
        } else if (binding.viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) {
            isBackButtonFragmentAlerts = hasBackButton
            stackFlagNotification = hasMenuItem
        }
        stackTitle = title
    }

    fun setBottomNavigationBadgeColor(color: Int, position: Int) {
        ahNotificationBuilder.setBackgroundColor(ContextCompat.getColor(this, color))
        val builder = ahNotificationBuilder.build()
        binding.bottomNavigationBTR.setNotification(builder, position)
    }

    fun viewPager(): AHBottomNavigationViewPager = binding.viewPagerBTR

    fun bottomNavigationBTR(): AHBottomNavigation = binding.bottomNavigationBTR

    fun btnHelp(): ConstraintLayout = binding.viewToolbar.btnHelp

    fun imageViewMarkAllAsRead(): ConstraintLayout = binding.viewToolbar.btnMarkAll

    fun viewApprovalsNavigation(): View =
        binding.viewApprovalsNavigation.viewApprovalsNavigationLayout

    fun textViewEditApprovals(): TextView = binding.viewToolbar.tvEdit

    fun btnEditApproval(): ConstraintLayout = binding.viewToolbar.btnEditApproval

    fun allowMultipleSelectionApprovals(allowMultipleSelectionApprovals: Boolean) {
        this.allowMultipleSelectionApprovals = allowMultipleSelectionApprovals
        if (binding.bottomNavigationBTR.currentItem == bottomNavigationItems[FRAGMENT_APPROVALS]) {
            binding.viewToolbar.btnEditApproval.setEnableView(allowMultipleSelectionApprovals)
        }
    }

    companion object {
        const val FRAGMENT_DASHBOARD = "dashboard"
        const val FRAGMENT_TRANSACT = "transact"
        const val FRAGMENT_APPROVALS = "approvals"
        const val FRAGMENT_SETTINGS = "settings"
        const val FRAGMENT_NOTIFICATIONS = "notifications"
        const val EXTRA_SWITCH_ORG = "from_switch_org"
        const val TAG_NEW_USER_DETECTED_DIALOG = "new_user_detected_dialog"
        const val TAG_TRUSTED_DEVICE_DIALOG = "trusted_device_dialog"
        const val TAG_READ_MCD_TERMS_DIALOG = "read_mcd_terms_dialog"
        const val TAG_TRUSTED_DEVICE_RESULT_DIALOG = "trusted_device_result_dialog"
        const val TAG_FINGERPRINT_DIALOG = "fingerprint_dialog"
        const val TAG_LOGOUT_DIALOG = "logout_dialog"
    }

    override val viewModelClassType: Class<DashboardViewModel>
        get() = DashboardViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityDashboardBinding
        get() = ActivityDashboardBinding::inflate
}
