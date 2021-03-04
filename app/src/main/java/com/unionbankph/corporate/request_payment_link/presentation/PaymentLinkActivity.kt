package com.unionbankph.corporate.request_payment_link.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_list.AccountFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.approval.presentation.ApprovalFragment
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogTabFragment
import com.unionbankph.corporate.settings.presentation.SettingsFragment
import com.unionbankph.corporate.transact.presentation.transact.TransactFragment
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.widget_badge_initial.*
import kotlinx.android.synthetic.main.widget_transparent_dashboard_appbar.*

class PaymentLinkActivity : AppCompatActivity(),
    AHBottomNavigation.OnTabSelectedListener{

    private var bottomNavigationItems: HashMap<String, Int> = hashMapOf()
    private var isBackButtonFragmentSettings: Boolean = false
    private var isBackButtonFragmentAlerts: Boolean = false
    private var hasNotificationLogs: Boolean = false
    private var adapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_link)

        textViewTitle.setText(R.string.title_generate_links)
        textViewTitle.setTextColor(ContextCompat.getColor  (this, R.color.colorWhite))

        textViewCorporationName.setText(R.string.title_corporation_name)
        btnRequestPayment()
        initBottomNavigation()
//        initViewPager()
//        enableTabs(isEnable = true)
    }

    private fun btnRequestPayment () {

        btnRequestPayment.setOnClickListener {

            val intent = Intent(this, RequestPaymentActivity::class.java)
            startActivity(intent)

        }
    }

    private fun initBottomNavigation() {
//        bottomNavigationItems[DashboardActivity.FRAGMENT_ACCOUNTS] = 0
//        bottomNavigationItems[DashboardActivity.FRAGMENT_TRANSACT] = 1
//        bottomNavigationItems[DashboardActivity.FRAGMENT_APPROVALS] = 2
//        bottomNavigationItems[DashboardActivity.FRAGMENT_NOTIFICATIONS] = 3
//        bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS] = 4
        val item1 =
            AHBottomNavigationItem(getString(R.string.title_tab_accounts), R.drawable.ic_accounts)
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
        bottomNavigationBTR?.defaultBackgroundColor =
            ContextCompat.getColor(this, R.color.colorWhite)
        bottomNavigationBTR?.accentColor = getColorFromAttr(R.attr.colorAccent)
        bottomNavigationBTR?.inactiveColor = ContextCompat.getColor(this, R.color.colorTextTab)
        bottomNavigationBTR?.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNavigationBTR?.setTitleTextSize(
            resources.getDimension(R.dimen.navigation_bottom_text_size_active),
            resources.getDimension(R.dimen.navigation_bottom_text_size_normal)
        )
        bottomNavigationBTR?.addItem(item1)
        bottomNavigationBTR?.addItem(item2)
        bottomNavigationBTR?.addItem(item3)
        bottomNavigationBTR?.addItem(item4)
        bottomNavigationBTR?.addItem(item5)
        bottomNavigationBTR?.currentItem = 1
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
                bottomNavigationBTR.getViewAtPosition(0).id = R.id.tabAccounts
                bottomNavigationBTR.getViewAtPosition(1).id = R.id.tabTransact
                bottomNavigationBTR.getViewAtPosition(2).id = R.id.tabApprovals
                bottomNavigationBTR.getViewAtPosition(3).id = R.id.tabProfile
                bottomNavigationBTR.getViewAtPosition(4).id = R.id.tabSettings
            }
        }
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        if (!wasSelected) {
//            viewModel.getOrganizationNotification(role?.organizationId.notNullable())
//            if ((isBackButtonFragmentSettings &&
//                        position == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS]) ||
//                (isBackButtonFragmentAlerts &&
//                        position == bottomNavigationItems[DashboardActivity.FRAGMENT_NOTIFICATIONS])
//            ) {
//                viewBadgeCount.visibility(false)
//                imageViewLogout.visibility(false)
//                imageViewMarkAllAsRead.visibility(false)
//                imageViewInitial.setImageResource(R.drawable.ic_arrow_back_white_24dp)
//                if (isSME) imageViewInitial.setColor(R.color.colorInfo)
//                textViewInitial.visibility = View.GONE
//                viewBadge.setOnClickListener {
//                    if (viewPagerBTR.currentItem == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS]) {
//                        popStackFragmentSettings()
//                    } else if (
//                        viewPagerBTR.currentItem == bottomNavigationItems[DashboardActivity.FRAGMENT_NOTIFICATIONS]) {
//                        popStackFragmentNotifications()
//                    }
//                }
//                textViewTitle.text = stackTitle
//            } else {
            if (viewPagerBTR.currentItem == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS] &&
                textViewInitial.visibility != View.VISIBLE
            ) {
                isBackButtonFragmentSettings = true
            } else if (
                viewPagerBTR.currentItem == bottomNavigationItems[DashboardActivity.FRAGMENT_NOTIFICATIONS] &&
                textViewInitial.visibility != View.VISIBLE
            ) {
                isBackButtonFragmentAlerts = true
            }
            imageViewMarkAllAsRead.visibility(
                position == bottomNavigationItems[DashboardActivity.FRAGMENT_NOTIFICATIONS] && hasNotificationLogs
            )
            imageViewInitial.setImageResource(R.drawable.circle_gradient_orange)

            if (viewPagerBTR.currentItem == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS]){
                viewPagerBTR.setOnClickListener{
                    val intent = Intent (this, SettingsFragment::class.java)
                    startActivity(intent)
                }
            }
//                if (isSME) imageViewInitial.clearTheme()
//                textViewInitial.visibility = View.VISIBLE
//                setOrganizationBadge(organizationBadgeCount)
//                viewBadge.setOnClickListener { navigateOrganizationScreen() }
//                textViewTitle.text = headerDashboard[position]
//            }
//            if (isSME) {
//                if (position == bottomNavigationItems[DashboardActivity.FRAGMENT_APPROVALS]) {
//                    removeElevation(viewToolbar)
//                } else {
//                    addElevation(viewToolbar)
//                }
//            }
//            textViewEditApprovals.visibility(
//                position == bottomNavigationItems[DashboardActivity.FRAGMENT_APPROVALS] &&
//                        allowMultipleSelectionApprovals
//            )
//            imageViewHelp.visibility(
//                position == bottomNavigationItems[DashboardActivity.FRAGMENT_ACCOUNTS] ||
//                        position == bottomNavigationItems[DashboardActivity.FRAGMENT_TRANSACT] ||
//                        position == bottomNavigationItems[DashboardActivity.FRAGMENT_APPROVALS] ||
//                        (position == bottomNavigationItems[DashboardActivity.FRAGMENT_NOTIFICATIONS] &&
//                                stackFlagNotification) ||
//                        (position == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS] &&
//                                stackFlagSettings)
//            )
//            if (position == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS]) {
//                val settingsFragment =
//                    adapter?.getItem(bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS]!!)!!
//                if (settingsFragment.isAdded) {
//                    val count = settingsFragment.childFragmentManager.backStackEntryCount
//                    imageViewLogout.visibility(count == 1)
//                }
//            } else {
//                imageViewLogout.visibility(false)
//            }
//            initForceTutorialTabs(position)
        }
//        else {
//            eventBus.settingsSyncEvent.emmit(
//                BaseEvent(SettingsSyncEvent.ACTION_SCROLL_TO_TOP)
//            )
//        }
        viewPagerBTR.currentItem = position
        return true
    }
//
//    private fun enableTabs(isEnable: Boolean) {
//        if (bottomNavigationBTR.getViewAtPosition(0) != null &&
//            bottomNavigationBTR.getViewAtPosition(1) != null &&
//            bottomNavigationBTR.getViewAtPosition(2) != null &&
//            bottomNavigationBTR.getViewAtPosition(3) != null &&
//            bottomNavigationBTR.getViewAtPosition(4) != null
//        ) {
//            bottomNavigationBTR.getViewAtPosition(0).isEnabled = isEnable
//            bottomNavigationBTR.getViewAtPosition(0).isClickable = isEnable
//            bottomNavigationBTR.getViewAtPosition(1).isEnabled = isEnable
//            bottomNavigationBTR.getViewAtPosition(1).isClickable = isEnable
//            bottomNavigationBTR.getViewAtPosition(2).isEnabled = isEnable
//            bottomNavigationBTR.getViewAtPosition(2).isClickable = isEnable
//            bottomNavigationBTR.getViewAtPosition(3).isEnabled = isEnable
//            bottomNavigationBTR.getViewAtPosition(3).isClickable = isEnable
//            bottomNavigationBTR.getViewAtPosition(4).isEnabled = isEnable
//            bottomNavigationBTR.getViewAtPosition(4).isClickable = isEnable
//        }
//    }
//
//    private fun initViewPager() {
//        adapter = ViewPagerAdapter(
//            supportFragmentManager
//        )
//        adapter?.addFragment(AccountFragment(), DashboardActivity.FRAGMENT_ACCOUNTS)
//        adapter?.addFragment(TransactFragment(), DashboardActivity.FRAGMENT_TRANSACT)
//        adapter?.addFragment(ApprovalFragment(), DashboardActivity.FRAGMENT_APPROVALS)
//        adapter?.addFragment(NotificationLogTabFragment(), DashboardActivity.FRAGMENT_NOTIFICATIONS)
//        adapter?.addFragment(SettingsFragment(), DashboardActivity.FRAGMENT_SETTINGS)
//        // viewPagerBTR.setPageTransformer(false, FadePageTransformer())
//        viewPagerBTR.setPagingEnabled(false)
//        viewPagerBTR.offscreenPageLimit = 4
//        viewPagerBTR.adapter = adapter
//    }
}