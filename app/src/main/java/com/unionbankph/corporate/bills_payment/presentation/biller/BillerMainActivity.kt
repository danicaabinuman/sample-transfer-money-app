package com.unionbankph.corporate.bills_payment.presentation.biller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.bills_payment.presentation.biller.biller_all.AllBillerFragment
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.FrequentBillerFragment
import com.unionbankph.corporate.databinding.ActivityBillersMainBinding

class BillerMainActivity :
    BaseActivity<ActivityBillersMainBinding, BillerMainViewModel>() {

    private var adapter: ViewPagerAdapter? = null

    var isInitialLoad = true

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        if (isSME) {
            removeElevation(binding.viewToolbar.appBarLayout)
            binding.shadowToolbar.isVisible = true
        }
        setToolbarTitle(
            binding.viewToolbar.tvToolbar,
            if (intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_FORM)
                getString(R.string.title_select_biller)
            else
                getString(R.string.title_payment_to)
        )
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupViewPager()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.tabLayoutBillers.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // onTabReselected
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // onTabReselected
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (viewUtil.isSoftKeyboardShown(binding.constraintLayoutBillers))
                    viewUtil.dismissKeyboard(this@BillerMainActivity)
                when (tab?.position) {
                    0 -> {
                        binding.viewPagerBillers.currentItem = 0
                    }
                    1 -> {
                        binding.viewPagerBillers.currentItem = 1
                    }
                }
            }
        })
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

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BILLS_PAYMENT_FORM) {
            binding.tabLayoutBillers.visibility = View.VISIBLE
            adapter?.addFragment(
                FrequentBillerFragment.newInstance(),
                FRAGMENT_FREQUENT_BILLERS
            )
        } else {
            binding.tabLayoutBillers.visibility = View.GONE
        }
        adapter?.addFragment(
            AllBillerFragment.newInstance(),
            FRAGMENT_ALL_BILLERS
        )
        binding.viewPagerBillers.offscreenPageLimit = 0
        binding.viewPagerBillers.currentItem = 1
        binding.viewPagerBillers.adapter = adapter
        binding.tabLayoutBillers.setupWithViewPager(binding.viewPagerBillers, false)
    }

    fun setCurrentViewPager(currentItem: Int) {
        binding.viewPagerBillers.currentItem = currentItem
    }

    companion object {
        const val EXTRA_PERMISSION = "permission"
        const val EXTRA_PAGE = "page"
        const val EXTRA_ACCOUNT_ID = "acccount_id"
        const val EXTRA_TYPE = "type"
        const val FRAGMENT_FREQUENT_BILLERS = "Frequent Billers"
        const val FRAGMENT_ALL_BILLERS = "All Billers"
        const val PAGE_FREQUENT_BILLER_FORM = "frequent_biller_form"
        const val PAGE_BILLS_PAYMENT_FORM = "bills_payment_form"
        const val PAGE_BILLS_PAYMENT_FILTER = "bills_payment_filter"
    }

    override val viewModelClassType: Class<BillerMainViewModel>
        get() = BillerMainViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBillersMainBinding
        get() = ActivityBillersMainBinding::inflate
}
