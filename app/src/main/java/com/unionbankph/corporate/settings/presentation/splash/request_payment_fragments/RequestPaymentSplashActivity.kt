package com.unionbankph.corporate.settings.presentation.splash.request_payment_fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.unionbankph.corporate.R

class RequestPaymentSplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment_splash_frame_screen)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager2)

        val fragments: ArrayList<Fragment> = arrayListOf(
            CreateUniquePaymentLinks(),
            AccessPaymentChannels(),
            EverythingInOnePlace()
        )

        val adapter = ViewPagerAdapter(fragments, this )
        viewPager.adapter = adapter
    }
}