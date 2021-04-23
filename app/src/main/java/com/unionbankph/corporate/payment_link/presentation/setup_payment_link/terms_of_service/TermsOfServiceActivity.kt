package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import kotlinx.android.synthetic.main.activity_terms_of_service.*

class TermsOfServiceActivity : BaseActivity<TermsOfServiceViewModel>(R.layout.activity_terms_of_service) {

    override fun onViewsBound() {
        super.onViewsBound()
        setUpTabs()
        focus()
        backButton()
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
            finish()
        }

    }

    private fun focus(){
        val tabIndex = intent.getStringExtra("termsAndConditions")
        if(tabIndex == "TC"){
            viewPagerTermsOfService.setCurrentItem(1)
        }

    }

    private fun backButton(){
        backButton.setOnClickListener(){
            finish()
        }
    }

}