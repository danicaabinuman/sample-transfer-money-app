package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.terms_of_service

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import kotlinx.android.synthetic.main.activity_terms_of_service.*

class TermsOfServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_service)

//        setViewPager()
        setUpTabs()

    }

    private fun setUpTabs(){
        val adapter = TermsOfServiceViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(FeeCharges(), "Fees & Charges")
        adapter.addFragment(TermsConditions(), "Terms & Conditions")
        viewPagerTermsOfService.adapter = adapter

        tlTermsOfService.setupWithViewPager(viewPagerTermsOfService)

        viewPagerTermsOfService.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        btnAgreeAndContinue.visibility = View.GONE
                    }
                    1 -> {
                        btnAgreeAndContinue.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }


        })

        btnAgreeAndContinue.setOnClickListener{
            val intent = Intent (this, SetupPaymentLinkActivity::class.java)
            startActivity(intent)
        }

    }

}