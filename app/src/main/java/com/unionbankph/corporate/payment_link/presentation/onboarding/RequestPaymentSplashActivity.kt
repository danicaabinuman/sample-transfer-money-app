package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.bus.event.TransactSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import kotlinx.android.synthetic.main.activity_request_payment_splash_frame_screen.*

class RequestPaymentSplashActivity : BaseActivity<RequestPaymentSplashViewModel>(R.layout.activity_request_payment_splash_frame_screen) {

    private var merchantExists = false
    private var fromWhatTab : String? = null

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[RequestPaymentSplashViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    fun initViews() {

        setViewPager()

        merchantExists = intent.getBooleanExtra(EXTRA_MERCHANT_EXISTS,false)
        fromWhatTab = intent.getStringExtra(EXTRA_FROM_WHAT_TAB)
        if(fromWhatTab == null){
            fromWhatTab = DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON
        }

        val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_ONBOARDED, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(SHAREDPREF_IS_ONBOARDED, false)){
            continueToNextScreen()
        } else {
            val editor = sharedPref.edit()
            editor.putBoolean(SHAREDPREF_IS_ONBOARDED, true)
            editor.apply()
        }

        nextPageBtn.setOnClickListener{
            viewPager2.currentItem = viewPager2.currentItem + 1
            when (viewPager2.currentItem) {
                0,1 -> {
                    llSkipAndNextBtn.visibility = View.VISIBLE
                    btnGetStarted.visibility = View.GONE
                }
                2 -> {
                    llSkipAndNextBtn.visibility = View.GONE
                    btnGetStarted.visibility = View.VISIBLE
                }
            }
        }

        btnGetStarted.setOnClickListener{
            continueToNextScreen()
        }
        skipBtn.setOnClickListener{
            continueToNextScreen()
        }
    }

    private fun setViewPager(){

        val fragments: ArrayList<Fragment> = arrayListOf(
            CreateUniquePaymentLinks(),
            AccessPaymentChannels(),
            EverythingInOnePlace()
        )

        val tabLayout = findViewById<TabLayout>(R.id.tlPageIndicator)
        val adapter = ViewPagerAdapter(fragments, this )
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2){
            tab, position ->
        }.attach()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0,1 -> {
                        llSkipAndNextBtn.visibility = View.VISIBLE
                        btnGetStarted.visibility = View.GONE
                    }
                    2 -> {
                        llSkipAndNextBtn.visibility = View.GONE
                        btnGetStarted.visibility = View.VISIBLE
                    }
                }
            }
        })
    }


    private fun continueToNextScreen(){

        if(fromWhatTab.equals(DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)){

            val intent = if(merchantExists){
                Intent(this, RequestForPaymentActivity::class.java)
            }else{
                Intent(this, SetupPaymentLinkActivity::class.java)
            }
            intent.putExtra(EXTRA_FROM_WHAT_TAB,fromWhatTab)
            startActivity(intent)
            finish()

        }else if(fromWhatTab.equals(DashboardViewModel.FROM_TRANSACT_TAB)){

            if(merchantExists){
                eventBus.transactSyncEvent.emmit(
                    BaseEvent(TransactSyncEvent.ACTION_GO_TO_PAYMENT_LINK_LIST)
                )
                finish()
            }else{
                val intent = Intent(this, SetupPaymentLinkActivity::class.java)
                intent.putExtra(EXTRA_FROM_WHAT_TAB,fromWhatTab)
                startActivity(intent)
                finish()
            }


        }


    }

    companion object {
        const val SHAREDPREF_IS_ONBOARDED = "sharedpref_is_onboarded"
        const val EXTRA_MERCHANT_EXISTS = "extra_merchant_exists"
        const val EXTRA_FROM_WHAT_TAB = "from_what_tab"
    }

}