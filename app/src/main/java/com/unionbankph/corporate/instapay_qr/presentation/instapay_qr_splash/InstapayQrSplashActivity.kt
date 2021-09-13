package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_splash

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityInstapayQrSplashBinding

class InstapayQrSplashActivity(
    override val bindingInflater: (LayoutInflater) -> ActivityInstapayQrSplashBinding,
    override val viewModelClassType: Class<InstapayQrSplashViewModel>
) :
    BaseActivity<ActivityInstapayQrSplashBinding, InstapayQrSplashViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    fun initViews(){

        setViewPager()

        binding.btnAllow.setOnClickListener {

            continueToNextScreen()

        }

        binding.btnNotNow.setOnClickListener {

            finish()

        }


    }

    fun setViewPager(){

        val fragments: ArrayList<Fragment> = arrayListOf(
            AllowPhoneCamera()
        )

        val tabLayout = findViewById<TabLayout>(R.id.tabIndicator)
        val adapter = InstapayViewPagerAdapter(fragments, this)
        binding.qrViewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, binding.qrViewPager2){
                tab, position ->
        }.attach()

        binding.qrViewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position) {
                    0,1 -> {
                        binding.btnAllow.visibility = View.VISIBLE
                        binding.btnNotNow.visibility = View.VISIBLE
                        binding.tabIndicator.visibility = View.GONE
                    }
                }
            }
        })
    }

    fun continueToNextScreen(){

    }


}