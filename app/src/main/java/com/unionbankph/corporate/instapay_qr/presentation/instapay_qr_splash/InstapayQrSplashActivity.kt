package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_splash

import android.Manifest
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityInstapayQrSplashBinding
import com.unionbankph.corporate.databinding.ActivityRequestPaymentSplashFrameScreenBinding
import com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_scanner.InstapayQrScannerActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashViewModel
import io.reactivex.rxkotlin.addTo

class InstapayQrSplashActivity :
    BaseActivity<ActivityInstapayQrSplashBinding, InstapayQrSplashViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()

    }

    fun initViews(){

        setViewPager()

        binding.btnAllow.setOnClickListener {

            initPermission()

        }

        binding.btnNotNow.setOnClickListener {

            finish()

        }
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    continueToNextScreen()
                } else {
                    initPermission()
                }
            }.addTo(disposables)
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

    private fun continueToNextScreen(){
        val intent = Intent(this, InstapayQrScannerActivity::class.java)
        startActivity(intent)
    }

    override val viewModelClassType: Class<InstapayQrSplashViewModel>
        get() = InstapayQrSplashViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityInstapayQrSplashBinding
        get() = ActivityInstapayQrSplashBinding::inflate

}