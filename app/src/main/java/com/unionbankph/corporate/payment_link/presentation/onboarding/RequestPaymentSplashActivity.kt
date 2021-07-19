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
import com.unionbankph.corporate.databinding.ActivityRequestPaymentSplashFrameScreenBinding
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity

class RequestPaymentSplashActivity :
    BaseActivity<ActivityRequestPaymentSplashFrameScreenBinding, RequestPaymentSplashViewModel>() {

    private var merchantExists = false
    private var fromWhatTab : String? = null

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

        binding.nextPageBtn.setOnClickListener{
            binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
            when (binding.viewPager2.currentItem) {
                0,1 -> {
                    binding.llSkipAndNextBtn.visibility = View.VISIBLE
                    binding.btnGetStarted.visibility = View.GONE
                }
                2 -> {
                    binding.llSkipAndNextBtn.visibility = View.GONE
                    binding.btnGetStarted.visibility = View.VISIBLE
                }
            }
        }

        binding.btnGetStarted.setOnClickListener{
            continueToNextScreen()
        }
        binding.skipBtn.setOnClickListener{
//            continueToNextScreen()
            when (binding.viewPager2.currentItem) {
                0 -> {
                    binding.viewPager2.currentItem = binding.viewPager2.currentItem + 2
                    binding.llSkipAndNextBtn.visibility = View.GONE
                    binding.btnGetStarted.visibility = View.VISIBLE
                }
                1 -> {
                    binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
                    binding.llSkipAndNextBtn.visibility = View.GONE
                    binding.btnGetStarted.visibility = View.VISIBLE
                }
                2 -> {
                    binding.llSkipAndNextBtn.visibility = View.GONE
                    binding.btnGetStarted.visibility = View.VISIBLE
                }
            }
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
        binding.viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, binding.viewPager2){
            tab, position ->
        }.attach()

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0,1 -> {
                        binding.llSkipAndNextBtn.visibility = View.VISIBLE
                        binding.btnGetStarted.visibility = View.GONE
                    }
                    2 -> {
                        binding.llSkipAndNextBtn.visibility = View.GONE
                        binding.btnGetStarted.visibility = View.VISIBLE
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

    override val layoutId: Int
        get() = R.layout.activity_request_payment_splash_frame_screen

    override val viewModelClassType: Class<RequestPaymentSplashViewModel>
        get() = RequestPaymentSplashViewModel::class.java

}