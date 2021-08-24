package com.unionbankph.corporate.app.dashboard

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.google.android.material.appbar.AppBarLayout
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
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogTabFragment
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.presentation.SettingsFragment
import com.unionbankph.corporate.settings.presentation.fingerprint.FingerprintBottomSheet
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.FeesAndChargesFragment
import com.unionbankph.corporate.transact.presentation.transact.TransactFragment
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.widget_badge_initial.*
import kotlinx.android.synthetic.main.widget_badge_small.*
import kotlinx.android.synthetic.main.widget_notification.*
import kotlinx.android.synthetic.main.widget_transparent_dashboard_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.collections.set

/**
 * Created by Herald Santos
 */
class DashboardActivity : BaseActivity<DashboardViewModel>(R.layout.activity_dashboard),
    AHBottomNavigation.OnTabSelectedListener,
    OnTutorialListener,
    OnConfirmationPageCallBack,
    FingerprintBottomSheet.OnFingerPrintListener {

    private val headerDashboard by lazyFast {
        resources.getStringArray(R.array.array_dashboard_header).toMutableList()
    }

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

    override fun onViewsBound() {
        super.onViewsBound()
        initViewPager()
        initBottomNavigation()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DashboardViewModel::class.java]
        viewModel.dashBoardState.observe(this, Observer {
            when (it) {
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
                    if (it.hasNotBiometric && RxFingerprint.isAvailable(this)) {
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
                    navigatePaymentLinkOnBoarding(it.merchantExists,it.fromWhatTab)
                }

                is Error -> {
                    handleOnError(it.throwable)
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
        RxView.clicks(imageViewLogout)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                showLogoutBottomSheet()
            }.addTo(disposables)
        imageViewHelp.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            when (bottomNavigationBTR.currentItem) {
                bottomNavigationItems[FRAGMENT_ACCOUNTS] -> {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_PUSH_TUTORIAL_ACCOUNT)
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

        btnRequestPayment.setOnClickListener{
            viewModel.validateMerchant(DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)
        }

        RxView.clicks(viewBadge)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateOrganizationScreen()
            }.addTo(disposables)

        RxView.clicks(viewNotificationBadge)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewPagerBTR.setCurrentItem(5, true)
                adapter?.notifyDataSetChanged()
            }.addTo(disposables)
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
                        viewBadge,
                        R.layout.frame_tutorial_upper_left,
                        getCircleFloatSize(viewBadge) -
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
                        bottomNavigationBTR.currentItem =
                            bottomNavigationItems[FRAGMENT_NOTIFICATIONS] ?: 5
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

        eventBus.transactSyncEvent.flowable.subscribe {
            when (it.eventType) {
                TransactSyncEvent.ACTION_VALIDATE_MERCHANT_EXIST -> {
                    viewModel.validateMerchant(DashboardViewModel.FROM_TRANSACT_TAB)
                }
                TransactSyncEvent.ACTION_GO_TO_PAYMENT_LINK_LIST -> {
                    viewPagerBTR.currentItem = bottomNavigationItems[FRAGMENT_DASHBOARD]!!
                    eventBus.transactSyncEvent.emmit(
                        BaseEvent(TransactSyncEvent.ACTION_REDIRECT_TO_PAYMENT_LINK_LIST)
                    )
                    setToolbarTitle(
                            getString(R.string.title_payment_links),
                            hasBackButton = false,
                            hasMenuItem = true
                    )
                    btnRequestPayment.visibility = View.VISIBLE
                }
            }
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

    private fun navigateToNotificationsFragment() {

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
    }

    private fun initDashboardViews(role: Role) {
        this.role = role
//        if (!isSME) {
            removeElevation(viewToolbar)
//        }
        role.let {
            textViewCorporationName?.text = it.organizationName
            textViewInitial?.text =
                viewUtil.getCorporateOrganizationInitial(it.organizationName)
            textViewTitle?.text = it.organizationName
            if (!it.hasApproval && !it.isApprover) {
                bottomNavigationBTR.disableItemAtPosition(2)
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
            adapter?.getItem(bottomNavigationItems[FRAGMENT_NOTIFICATIONS] ?: 5)!!
        notificationTabFragment.childFragmentManager.popBackStackImmediate()
        val fragmentManager = notificationTabFragment.childFragmentManager
        val fragmentTag = notificationTabFragment
            .childFragmentManager
            .getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
            .name
        when (fragmentTag) {
            NotificationLogTabFragment.FRAGMENT_NOTIFICATION_LOG_DETAIL -> {
                imageViewMarkAllAsRead.visibility(false)
                setToolbarTitle(
                    getString(
                        R.string.title_dashboard_header_notifications
                    ),
                    hasBackButton = true,
                    hasMenuItem = false
                )
            }
            else -> {
                imageViewMarkAllAsRead.visibility(hasNotificationLogs)
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

    override fun onBackPressed() {
        if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) {
            val settingsFragment =
                adapter?.getItem(bottomNavigationItems[FRAGMENT_SETTINGS]!!)!!
            if (settingsFragment.isAdded) {
                val count = settingsFragment.childFragmentManager.backStackEntryCount
                if (count == 1 ||
                    viewPagerBTR.currentItem != bottomNavigationItems[FRAGMENT_SETTINGS]
                ) {
                    showLogoutBottomSheet()
                } else {
                    popStackFragmentSettings()
                }
            }
        } else if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) {
            val notificationTabFragment =
                adapter?.getItem(bottomNavigationItems[FRAGMENT_NOTIFICATIONS] ?: 5)!!
            if (notificationTabFragment.isAdded) {
                val count = notificationTabFragment.childFragmentManager.backStackEntryCount
                if (count == 1 ||
                    viewPagerBTR.currentItem != bottomNavigationItems[FRAGMENT_NOTIFICATIONS]
                ) {
                    showLogoutBottomSheet()
                } else {
                    popStackFragmentNotifications()
                }
            }
        } else if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_DASHBOARD]) {
            val transactTabFragment = adapter?.getItem(bottomNavigationItems[FRAGMENT_DASHBOARD] ?: 1)!!
            if (transactTabFragment.isAdded) {

                btnRequestPayment.visibility = View.GONE
                val count = transactTabFragment.childFragmentManager.backStackEntryCount
                if (count == 0 || viewPagerBTR.currentItem != bottomNavigationItems[FRAGMENT_DASHBOARD]){
                    showLogoutBottomSheet()
                } else {
                    val fragmentManager = transactTabFragment.childFragmentManager
                    val fragmentTag = transactTabFragment
                        .childFragmentManager
                        .getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
                        .name
                    if (fragmentTag.equals(TransactFragment.FRAGMENT_REQUEST_PAYMENT,true)) {
                        transactTabFragment.childFragmentManager.popBackStackImmediate()
                        setToolbarTitle(
                            getString(R.string.title_dashboard_header_transact),
                            hasBackButton = true,
                            hasMenuItem = true
                        )
                    }
                }

            }
        } else {
            if (viewApprovalsNavigation.visibility == View.VISIBLE) {
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
                        position == bottomNavigationItems[FRAGMENT_NOTIFICATIONS])
            ) {
                viewBadgeCount.visibility(false)
                imageViewLogout.visibility(false)
                imageViewMarkAllAsRead.visibility(false)
                btnRequestPayment.visibility(false)
                imageViewInitial.setImageResource(R.drawable.ic_arrow_back_white_24dp)
                if (isSME) imageViewInitial.setColor(R.color.colorInfo)
                textViewInitial.visibility = View.GONE
                viewBadge.setOnClickListener {
                    if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) {
                        popStackFragmentSettings()
                    } else if (
                        viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) {
                        popStackFragmentNotifications()
                    }
                }
                textViewTitle.text = stackTitle
            } else {
                if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS] &&
                    textViewInitial.visibility != View.VISIBLE
                ) {
                    isBackButtonFragmentSettings = true
                } else if (
                    viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS] &&
                    textViewInitial.visibility != View.VISIBLE
                ) {
                    isBackButtonFragmentAlerts = true
                }
                imageViewMarkAllAsRead.visibility(
                    position == bottomNavigationItems[FRAGMENT_NOTIFICATIONS] && hasNotificationLogs
                )
                imageViewInitial.setImageResource(R.drawable.circle_medium_gradient_orange)
                if (isSME) imageViewInitial.clearTheme()
                textViewInitial.visibility = View.VISIBLE
                setOrganizationBadge(organizationBadgeCount)
                viewBadge.setOnClickListener { navigateOrganizationScreen() }
                textViewTitle.text = when (position == 0) {
                    true -> role?.organizationName
                    else -> headerDashboard[position]
                }
            }
            if (isSME) {
                if (position == bottomNavigationItems[FRAGMENT_APPROVALS]) {
                    removeElevation(viewToolbar)
                } else {
//                    addElevation(viewToolbar)
                }
            }
            textViewEditApprovals.visibility(
                position == bottomNavigationItems[FRAGMENT_APPROVALS] &&
                        allowMultipleSelectionApprovals
            )
            imageViewHelp.visibility(
                position == bottomNavigationItems[FRAGMENT_DASHBOARD] ||
                position == bottomNavigationItems[FRAGMENT_ACCOUNTS] ||
                        position == bottomNavigationItems[FRAGMENT_APPROVALS] ||
                        (position == bottomNavigationItems[FRAGMENT_NOTIFICATIONS] &&
                                stackFlagNotification) ||
                        (position == bottomNavigationItems[FRAGMENT_SETTINGS] &&
                                stackFlagSettings)
            )

            btnRequestPayment.visibility(
                position == bottomNavigationItems[FRAGMENT_ACCOUNTS]
            )
            if (position == bottomNavigationItems[FRAGMENT_SETTINGS]) {
                val settingsFragment =
                    adapter?.getItem(bottomNavigationItems[FRAGMENT_SETTINGS]!!)!!
                if (settingsFragment.isAdded) {
                    val count = settingsFragment.childFragmentManager.backStackEntryCount
                    imageViewLogout.visibility(count == 1)
                }
            } else {
                imageViewLogout.visibility(false)
            }
            initForceTutorialTabs(position)
        } else {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_SCROLL_TO_TOP)
            )
        }
        viewPagerBTR.currentItem = position
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
                imageViewHelp,
                imageViewLogout,
                viewBadge -> {
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
                    val marginLeft = if (view == viewBadge) {
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
                    imageViewHelp -> {
                        viewModel.setTutorialIntroduction(false)
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                    viewBadge -> {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_BOTTOM)
                        )
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                    imageViewLogout -> {
                        viewModel.setTutorialUser(false)
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
            } else {
                tutorialEngineUtil.startTutorial(
                    this,
                    imageViewHelp,
                    R.layout.frame_tutorial_upper_right,
                    getCircleFloatSize(imageViewHelp),
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

        if (fragment == FRAGMENT_NOTIFICATIONS) {
            imageViewNotificationBadgeIndicator.visibility = when (badgeCount > 0) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            return
        }

        setBottomNavigationBadge(
            badgeCount, bottomNavigationItems[fragment]!!,
            if (isColored) R.color.colorRedBadge else R.color.colorGrayBadge
        )
    }

    private fun initForceTutorialTabs(position: Int) {
        when (position) {
            bottomNavigationItems[FRAGMENT_ACCOUNTS] -> {
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
        bottomNavigationBTR?.setNotification(builder, position)
    }

    private fun setOrganizationBadge(badgeCount: BadgeCount?) {
        if (badgeCount == null) return
        if (viewBadgeCount.visibility == View.VISIBLE) {
            viewBadgeCount.visibility(badgeCount.badge > 0 && textViewInitial.visibility == View.VISIBLE)
        } else {
            if (textViewInitial.visibility == View.VISIBLE && badgeCount.badge > 0)
                viewUtil.startAnimateView(true, viewBadgeCount, R.anim.anim_popup)
        }
        textViewBadgeCount.text = badgeCount.badge.toString()
        textViewBadgeCount.setBackgroundResource(
            if (!badgeCount.isColored)
                R.drawable.circle_gray_badge
            else
                R.drawable.circle_red_badge
        )
    }

    private fun initBottomNavigation() {
        bottomNavigationItems[FRAGMENT_DASHBOARD] = 0
        bottomNavigationItems[FRAGMENT_ACCOUNTS] = 1
        bottomNavigationItems[FRAGMENT_APPROVALS] = 2
        bottomNavigationItems[FRAGMENT_PAY_BILLS] = 3
        bottomNavigationItems[FRAGMENT_SETTINGS] = 4
        bottomNavigationItems[FRAGMENT_NOTIFICATIONS] = 5
        val item1 = AHBottomNavigationItem(
            getString(R.string.title_tab_dashboard),
            R.drawable.ic_vector_dashboard_dashboard
        )
        val item2 = AHBottomNavigationItem(
            getString(R.string.title_tab_accounts), R.drawable.ic_vector_dashboard_accounts
        )
        val item3 = AHBottomNavigationItem(
            getString(R.string.title_tab_approvals),
            R.drawable.ic_vector_dashboard_approvals
        )
        val item4 = AHBottomNavigationItem(
            getString(R.string.title_tab_pay_bills),
            R.drawable.ic_vector_dashboard_pay_bills
        )
        val item5 = AHBottomNavigationItem(
            getString(R.string.title_tab_settings),
            R.drawable.ic_vector_dashboard_settings
        )

        bottomNavigationBTR?.defaultBackgroundColor =
            ContextCompat.getColor(this, R.color.colorWhite)
        bottomNavigationBTR?.accentColor = getColorFromAttr(R.attr.colorAccent)
        bottomNavigationBTR?.inactiveColor = ContextCompat.getColor(this, R.color.dsColorDarkGray)
        bottomNavigationBTR?.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNavigationBTR?.setTitleTextSize(
            resources.getDimension(R.dimen.navigation_bottom_text_size_normal),
            resources.getDimension(R.dimen.navigation_bottom_text_size_normal)
        )
        bottomNavigationBTR?.addItem(item1)
        bottomNavigationBTR?.addItem(item2)
        bottomNavigationBTR?.addItem(item3)
        bottomNavigationBTR?.addItem(item4)
        bottomNavigationBTR?.addItem(item5)
        bottomNavigationBTR?.currentItem = 0
        bottomNavigationBTR.setNotificationMarginLeft(
            resources.getDimension(R.dimen.bottom_notification_margin).toInt(),
            resources.getDimension(R.dimen.bottom_notification_margin).toInt()
        )
        bottomNavigationBTR?.setOnTabSelectedListener(this)
        bottomNavigationBTR.post {
            if (bottomNavigationBTR.getViewAtPosition(0) != null &&
                bottomNavigationBTR.getViewAtPosition(1) != null &&
                bottomNavigationBTR.getViewAtPosition(2) != null &&
                bottomNavigationBTR.getViewAtPosition(3) != null &&
                bottomNavigationBTR.getViewAtPosition(4) != null
            ) {
                bottomNavigationBTR.getViewAtPosition(0).id = R.id.tabDashboard
                bottomNavigationBTR.getViewAtPosition(1).id = R.id.tabAccounts
                bottomNavigationBTR.getViewAtPosition(2).id = R.id.tabApprovals
                bottomNavigationBTR.getViewAtPosition(3).id = R.id.tabPayBills
                bottomNavigationBTR.getViewAtPosition(4).id = R.id.tabSettings

                // Disable Pay Bills Temporarily
                bottomNavigationBTR.getViewAtPosition(3).isEnabled = false
                bottomNavigationBTR.getViewAtPosition(3).isClickable = false
            }
        }
    }

    private fun enableTabs(isEnable: Boolean) {
        if (bottomNavigationBTR.getViewAtPosition(0) != null &&
            bottomNavigationBTR.getViewAtPosition(1) != null &&
            bottomNavigationBTR.getViewAtPosition(2) != null &&
            bottomNavigationBTR.getViewAtPosition(3) != null &&
            bottomNavigationBTR.getViewAtPosition(4) != null
        ) {
            bottomNavigationBTR.getViewAtPosition(0).isEnabled = isEnable
            bottomNavigationBTR.getViewAtPosition(0).isClickable = isEnable
            bottomNavigationBTR.getViewAtPosition(1).isEnabled = isEnable
            bottomNavigationBTR.getViewAtPosition(1).isClickable = isEnable
            bottomNavigationBTR.getViewAtPosition(2).isEnabled = isEnable
            bottomNavigationBTR.getViewAtPosition(2).isClickable = isEnable
            bottomNavigationBTR.getViewAtPosition(3).isEnabled = false
            bottomNavigationBTR.getViewAtPosition(3).isClickable = false
            bottomNavigationBTR.getViewAtPosition(4).isEnabled = isEnable
            bottomNavigationBTR.getViewAtPosition(4).isClickable = isEnable
        }
    }

    private fun initViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter?.addFragment(DashboardFragment(), FRAGMENT_DASHBOARD)
        adapter?.addFragment(AccountFragment(), FRAGMENT_ACCOUNTS)
        adapter?.addFragment(ApprovalFragment(), FRAGMENT_APPROVALS)
        adapter?.addFragment(FeesAndChargesFragment(), FRAGMENT_PAY_BILLS)
        adapter?.addFragment(SettingsFragment(), FRAGMENT_SETTINGS)
        adapter?.addFragment(NotificationLogTabFragment(), FRAGMENT_NOTIFICATIONS)
        // viewPagerBTR.setPageTransformer(false, FadePageTransformer())
        viewPagerBTR.setPagingEnabled(false)
        viewPagerBTR.offscreenPageLimit = 5
        viewPagerBTR.adapter = adapter
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
        fingerprintBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_fingerprint_white_48dp,
            getString(R.string.title_enable_fingerprint),
            getString(R.string.desc_fingerprint),
            getString(R.string.action_link),
            getString(R.string.action_not_now)
        )
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
            imageViewLogout,
            R.layout.frame_tutorial_upper_right,
            getCircleFloatSize(imageViewLogout),
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

    fun setToolbarTitle(title: String, hasBackButton: Boolean, hasMenuItem: Boolean = false) {
        textViewTitle?.text = title
        imageViewHelp.visibility(hasMenuItem)
        if (!hasBackButton) {
            imageViewLogout.visibility(
                viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]
            )
            imageViewInitial.setImageResource(R.drawable.circle_medium_gradient_orange)
            if (isSME) imageViewInitial.clearTheme()
            viewBadge.setOnClickListener { navigateOrganizationScreen() }
            textViewInitial.visibility = View.VISIBLE
            setOrganizationBadge(organizationBadgeCount)
        } else {
            viewBadgeCount.visibility(false)
            imageViewLogout.visibility(false)
            imageViewInitial.setImageResource(R.drawable.ic_arrow_back_white_24dp)
            if (isSME) imageViewInitial.setColor(R.color.colorInfo)
            viewBadge.setOnClickListener {
                if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) {
                    popStackFragmentSettings()
                } else if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) {
                    popStackFragmentNotifications()
                }
            }
            textViewInitial.visibility = View.GONE
        }
        if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_SETTINGS]) {
            isBackButtonFragmentSettings = hasBackButton
            stackFlagSettings = hasMenuItem
        } else if (viewPagerBTR.currentItem == bottomNavigationItems[FRAGMENT_NOTIFICATIONS]) {
            isBackButtonFragmentAlerts = hasBackButton
            stackFlagNotification = hasMenuItem
        }
        stackTitle = title
    }

    fun setBottomNavigationBadgeColor(color: Int, position: Int) {
        ahNotificationBuilder.setBackgroundColor(ContextCompat.getColor(this, color))
        val builder = ahNotificationBuilder.build()
        bottomNavigationBTR?.setNotification(builder, position)
    }

    fun viewPager(): AHBottomNavigationViewPager = viewPagerBTR

    fun bottomNavigationBTR(): AHBottomNavigation = bottomNavigationBTR

    fun imageViewHelp(): ImageView = imageViewHelp

    fun imageViewMarkAllAsRead(): ImageView = imageViewMarkAllAsRead

    fun viewApprovalsNavigation(): View = viewApprovalsNavigation

    fun textViewEditApprovals(): AppCompatTextView = textViewEditApprovals

    fun getRole() : Role? {
        return  this.role
    }

    fun allowMultipleSelectionApprovals(allowMultipleSelectionApprovals: Boolean) {
        this.allowMultipleSelectionApprovals = allowMultipleSelectionApprovals
        if (bottomNavigationBTR.currentItem == bottomNavigationItems[FRAGMENT_APPROVALS]) {
            textViewEditApprovals.visibility(allowMultipleSelectionApprovals)
        }
    }

    companion object {
        const val FRAGMENT_DASHBOARD = "dashboard"
        const val FRAGMENT_ACCOUNTS = "accounts"
        const val FRAGMENT_TRANSACT = "transact"
        const val FRAGMENT_APPROVALS = "approvals"
        const val FRAGMENT_SETTINGS = "settings"
        const val FRAGMENT_NOTIFICATIONS = "notifications"
        const val FRAGMENT_PAY_BILLS = "notifications"
        const val EXTRA_SWITCH_ORG = "from_switch_org"
        const val TAG_NEW_USER_DETECTED_DIALOG = "new_user_detected_dialog"
        const val TAG_TRUSTED_DEVICE_DIALOG = "trusted_device_dialog"
        const val TAG_READ_MCD_TERMS_DIALOG = "read_mcd_terms_dialog"
        const val TAG_TRUSTED_DEVICE_RESULT_DIALOG = "trusted_device_result_dialog"
        const val TAG_FINGERPRINT_DIALOG = "fingerprint_dialog"
        const val TAG_LOGOUT_DIALOG = "logout_dialog"
    }
}
