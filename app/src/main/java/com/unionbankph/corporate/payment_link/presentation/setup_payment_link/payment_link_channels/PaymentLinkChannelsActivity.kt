package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_payment_link_channels.*

class PaymentLinkChannelsActivity :
    BaseActivity<PaymentLinkChannelsViewModel>(R.layout.activity_payment_link_channels) {

    override fun onViewsBound() {
        super.onViewsBound()

        setupTabs()
    }

    private fun setupTabs() {
        val adapter = TabLayoutAdapter(supportFragmentManager)
        adapter.addFragment(PaymentMethodsFragment(), getString(R.string.title_payment_methods))
        adapter.addFragment(FeesAndChargesFragment(), getString(R.string.title_fees_charges))
        viewPagerPaymentLinkChannels.adapter = adapter

        tlPaymentLinkChannels.setupWithViewPager(viewPagerPaymentLinkChannels)

        viewPagerPaymentLinkChannels.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

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
}