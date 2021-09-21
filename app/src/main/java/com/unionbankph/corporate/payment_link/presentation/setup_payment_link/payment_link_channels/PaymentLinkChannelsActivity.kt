package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityPaymentLinkChannelsBinding
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.CardAcceptanceOptionActivity

class PaymentLinkChannelsActivity :
    BaseActivity<ActivityPaymentLinkChannelsBinding, PaymentLinkChannelsViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupTabs()
        nextButton()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupTabs() {
        val adapter = TabLayoutAdapter(supportFragmentManager)
        adapter.addFragment(PaymentMethodsFragment(), getString(R.string.title_payment_methods))
        adapter.addFragment(FeesAndChargesFragment(), getString(R.string.fees_and_charges))
        binding.viewPagerPaymentLinkChannels.adapter = adapter

        binding.viewToolbar.tabLayout.setupWithViewPager(binding.viewPagerPaymentLinkChannels)

        binding.viewPagerPaymentLinkChannels.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> { }
                    1 -> { }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }


        })
    }

    private fun nextButton() {
        binding.btnNext.setOnClickListener {

            val bundle = Bundle()
            // bundle.putString( ) Todo() Setup Bundle

            navigator.navigate(
                this,
                CardAcceptanceOptionActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
    }


    class TabLayoutAdapter(supportFragmentManager: FragmentManager) :
        FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList : ArrayList<Fragment> = arrayListOf()
        private val fragmentTitle : ArrayList<String> = arrayListOf()

        override fun getCount(): Int = fragmentList.size

        override fun getItem(position: Int): Fragment = fragmentList[position]

        override fun getPageTitle(position: Int): CharSequence = fragmentTitle[position]

        fun addFragment(fragment : Fragment, title : String){
            fragmentList.add(fragment)
            fragmentTitle.add(title)
        }
    }

    override val bindingInflater: (LayoutInflater) -> ActivityPaymentLinkChannelsBinding
        get() = ActivityPaymentLinkChannelsBinding::inflate

    override val viewModelClassType: Class<PaymentLinkChannelsViewModel>
        get() = PaymentLinkChannelsViewModel::class.java
}