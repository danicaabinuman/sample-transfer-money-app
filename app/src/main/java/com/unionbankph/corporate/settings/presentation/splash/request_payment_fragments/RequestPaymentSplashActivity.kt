package com.unionbankph.corporate.settings.presentation.splash.request_payment_fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.unionbankph.corporate.R
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestPaymentActivity
import com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.SetupPaymentLink
import kotlinx.android.synthetic.main.activity_request_payment_splash_frame_screen.*

class RequestPaymentSplashActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "request_payment_link"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment_splash_frame_screen)

        skipToRequestPayment()
        setViewPager()
        getStarted()

        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(PREF_NAME, false)){
            val intent = Intent(this, SetupPaymentLink::class.java)
            startActivity(intent)
            finish()
        } else {
//            setContentView(R.layout.activity_request_payment_splash_frame_screen)
            setViewPager()
            val editor = sharedPref.edit()
            editor.putBoolean(PREF_NAME, true)
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


//        val viewPager: ViewPager2 = findViewById(R.id.viewPager2)
//
//        val fragments: ArrayList<Fragment> = arrayListOf(
//            CreateUniquePaymentLinks(),
//            AccessPaymentChannels(),
//            EverythingInOnePlace()
//        )
//
//        val adapter = ViewPagerAdapter(fragments, this )
//        viewPager.adapter = adapter
    }

    private fun setViewPager(){

        val fragments: ArrayList<Fragment> = arrayListOf(
            CreateUniquePaymentLinks(),
            AccessPaymentChannels(),
            EverythingInOnePlace()
        )

        val adapter = ViewPagerAdapter(fragments, this )
        viewPager2.adapter = adapter

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

    private fun getStarted(){
        btnGetStarted.setOnClickListener{
            val intent = Intent(this, SetupPaymentLink::class.java)
            startActivity(intent)
        }
    }

    private fun skipToRequestPayment(){
        skipBtn.setOnClickListener{
            val intent = Intent(this, SetupPaymentLink::class.java)
            startActivity(intent)
        }
    }

}