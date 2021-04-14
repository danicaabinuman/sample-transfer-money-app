package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.unionbankph.corporate.R
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import kotlinx.android.synthetic.main.activity_request_payment_splash_frame_screen.*

class RequestPaymentSplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment_splash_frame_screen)

        skipToRequestPayment()
        setViewPager()
        getStarted()

        val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_ONBOARDED, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(SHAREDPREF_IS_ONBOARDED, false)){
            val intent = Intent(this, SetupPaymentLinkActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            setViewPager()
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
            val intent = Intent(this, SetupPaymentLinkActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun skipToRequestPayment(){
        skipBtn.setOnClickListener{
            val intent = Intent(this, SetupPaymentLinkActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val SHAREDPREF_IS_ONBOARDED = "sharedpref_is_onboarded"
    }

}