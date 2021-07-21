package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityTermsOfServiceBinding
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity

class TermsOfServiceActivity : BaseActivity<ActivityTermsOfServiceBinding, TermsOfServiceViewModel>() {

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
        binding.viewPagerTermsOfService.adapter = adapter

        binding.tlTermsOfService.setupWithViewPager(binding.viewPagerTermsOfService)

        binding.viewPagerTermsOfService.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.btnAgreeAndContinue.visibility = View.GONE
                    }
                    1 -> {
                        binding.btnAgreeAndContinue.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }


        })

        binding.btnAgreeAndContinue.setOnClickListener{
            finish()
        }

    }

    private fun focus(){
        val tabIndex = intent.getStringExtra("termsAndConditions")
        if(tabIndex == "TC"){
            binding.viewPagerTermsOfService.setCurrentItem(1)
        }

    }

    private fun backButton(){
        binding.backButton.setOnClickListener(){
            finish()
        }
    }

    override val layoutId: Int
        get() = R.layout.activity_terms_of_service

    override val viewModelClassType: Class<TermsOfServiceViewModel>
        get() = TermsOfServiceViewModel::class.java

}