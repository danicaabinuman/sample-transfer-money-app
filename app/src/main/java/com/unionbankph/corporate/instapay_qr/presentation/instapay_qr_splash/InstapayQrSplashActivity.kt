package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
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
        sharedPreference()
        skipButton()

        binding.btnAllow.setOnClickListener {
            initPermission()
        }

        binding.btnNotNow.setOnClickListener {
            onBackPressed()
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
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

    private fun skipButton(){
        binding.btnSkip.setOnClickListener {
            when(binding.qrViewPager2.currentItem) {
                0 -> {
                    binding.qrViewPager2.currentItem = binding.qrViewPager2.currentItem + 2
                }
                1 -> {
                    binding.qrViewPager2.currentItem = binding.qrViewPager2.currentItem + 1
                }
            }
        }
    }

    private fun sharedPreference(){
        val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_ONBOARDED, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(SHAREDPREF_IS_ONBOARDED, false)){
            continueToNextScreen()
            finish()

        } else {
            val editor = sharedPref.edit()
            editor.putBoolean(SHAREDPREF_IS_ONBOARDED, true)
            editor.apply()
        }
    }

    private fun setViewPager(){

        val fragments: ArrayList<Fragment> = arrayListOf(
            TransferFunds(),
            GenerateQrCode(),
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
                    0 -> {
                        binding.btnAllow.visibility = View.GONE
                        binding.btnNotNow.visibility = View.GONE
                        binding.tabIndicator.visibility = View.VISIBLE
                        binding.tvDetail.text = "Transfer funds easily by scanning any Instapay QR codes."
                        binding.btnSkip.visibility = View.VISIBLE
                    }
                    1 -> {
                        binding.btnAllow.visibility = View.GONE
                        binding.btnNotNow.visibility = View.GONE
                        binding.tabIndicator.visibility = View.VISIBLE
                        binding.tvDetail.text = "Generate your own QR Codes to conveniently receive Instapay Fund Transfers."
                        binding.btnSkip.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.btnAllow.visibility = View.VISIBLE
                        binding.btnNotNow.visibility = View.VISIBLE
                        binding.tabIndicator.visibility = View.GONE
                        binding.tvDetail.text = "Allow us to use your phone’s camera to scan other Instapay QR Codes"
                        binding.btnSkip.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun continueToNextScreen(){
        val intent = Intent(this, InstapayQrScannerActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val SHAREDPREF_IS_ONBOARDED = "sharedpref_is_onboarded"
    }

    override val viewModelClassType: Class<InstapayQrSplashViewModel>
        get() = InstapayQrSplashViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityInstapayQrSplashBinding
        get() = ActivityInstapayQrSplashBinding::inflate

}