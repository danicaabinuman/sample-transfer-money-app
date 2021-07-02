package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_payment_link_channels.*
import kotlinx.android.synthetic.main.activity_payment_link_channels.viewToolbar
import kotlinx.android.synthetic.main.widget_transparent_org_appbar_with_tab_layout.*

class PaymentLinkChannelsActivity :
    BaseActivity<PaymentLinkChannelsViewModel>(R.layout.activity_payment_link_channels) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange)
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
        viewPagerPaymentLinkChannels.adapter = adapter

        tabLayout.setupWithViewPager(viewPagerPaymentLinkChannels)

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

    private fun nextButton() {
        btnNext.setOnClickListener {
            TODO() // Set Destination Activity
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
}