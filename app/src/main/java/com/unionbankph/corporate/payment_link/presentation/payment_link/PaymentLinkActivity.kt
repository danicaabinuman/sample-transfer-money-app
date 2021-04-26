package com.unionbankph.corporate.payment_link.presentation.payment_link

import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.settings.presentation.SettingsFragment
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.bottomNavigationBTR
import kotlinx.android.synthetic.main.activity_nominate_settlement.*
import kotlinx.android.synthetic.main.activity_payment_link.*
import kotlinx.android.synthetic.main.activity_setup_payment_links.*
import kotlinx.android.synthetic.main.widget_badge_initial.*
import kotlinx.android.synthetic.main.widget_transparent_dashboard_appbar.*


class PaymentLinkActivity : BaseActivity<PaymentLinkViewModel>(R.layout.activity_payment_link),AHBottomNavigation.OnTabSelectedListener {

    private var bottomNavigationItems: HashMap<String, Int> = hashMapOf()
    private var isBackButtonFragmentSettings: Boolean = false
    private var isBackButtonFragmentAlerts: Boolean = false
    private var hasNotificationLogs: Boolean = false
    private var adapter: ViewPagerAdapter? = null

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[PaymentLinkViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupInputs()
        setupOutputs()
    }

    private fun initViews(){
        textViewTitle.setText(R.string.title_generate_links)
        textViewTitle.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))

        editTextSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                flLoading.visibility = View.VISIBLE
                viewModel.doSearch(editTextSearch.text.toString())
                return@OnEditorActionListener true
            }
            false
        })

        textViewCorporationName.setText(R.string.title_corporation_name)
        btnRequestPayment()
        initBottomNavigation()
    }

    private fun setupInputs(){
        flLoading.visibility = View.VISIBLE
        viewModel.getAllPaymentLinks()
    }
    private fun setupOutputs(){
        viewModel.paymentLinkListPaginatedResponse.observe(this, Observer {
            flLoading.visibility = View.GONE
            updateRecyclerView(it.data)
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        flLoading.visibility = View.GONE
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }

    private fun updateRecyclerView(data: List<PaymentLinkModel>?) {
        val paymentLinksAdapter = PaymentLinkAdapter(data as MutableList<PaymentLinkModel>)
        rvPaymentLinkList.adapter = paymentLinksAdapter
        paymentLinksAdapter.onItemClick = {
            it.referenceNo?.let { it1 -> showPaymentLinkDetails(it1) }
        }
    }


    private fun showPaymentLinkDetails(referenceNo: String){
        val intent = Intent(this@PaymentLinkActivity, BillingDetailsActivity::class.java)
        intent.putExtra(BillingDetailsActivity.EXTRA_REFERENCE_NUMBER,referenceNo)
        startActivity(intent)
    }

    private fun btnRequestPayment() {

        btnRequestPayment.setOnClickListener {
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initBottomNavigation() {

        val item1 =
                AHBottomNavigationItem(
                    getString(R.string.title_tab_accounts),
                    R.drawable.ic_accounts
                )
        val item2 = AHBottomNavigationItem(
            getString(R.string.title_tab_transact), R.drawable.ic_send_request
        )
        val item3 =
                AHBottomNavigationItem(
                    getString(R.string.title_tab_approvals),
                    R.drawable.ic_approval
                )
        val item4 = AHBottomNavigationItem(
            getString(R.string.title_tab_notifications),
            R.drawable.ic_notification
        )
        val item5 =
                AHBottomNavigationItem(
                    getString(R.string.title_tab_settings),
                    R.drawable.ic_settings
                )
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

            if (viewPagerBTR.currentItem == bottomNavigationItems[DashboardActivity.FRAGMENT_SETTINGS]) {
                viewPagerBTR.setOnClickListener {
                    val intent = Intent(this, SettingsFragment::class.java)
                    startActivity(intent)
                }
            }
        }
        viewPagerBTR.currentItem = position
        return true
    }
}