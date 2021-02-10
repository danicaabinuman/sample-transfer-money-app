package com.unionbankph.corporate.bills_payment.presentation.biller

import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_billers_main.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class BillerMainActivity : BaseActivity<BillerMainViewModel>(R.layout.activity_billers_main) {

    private var adapter: ViewPagerAdapter? = null

    var isInitialLoad = true

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        if (isSME) {
            removeElevation(viewToolbar)
            shadow_toolbar.isVisible = true
        }
        setToolbarTitle(
            tvToolbar,
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
        viewModel = ViewModelProviders.of(this, viewModelFactory)[BillerMainViewModel::class.java]
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        tabLayoutBillers.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // onTabReselected
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // onTabReselected
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (viewUtil.isSoftKeyboardShown(constraintLayoutBillers))
                    viewUtil.dismissKeyboard(this@BillerMainActivity)
                when (tab?.position) {
                    0 -> {
                        viewPagerBillers.currentItem = 0
                    }
                    1 -> {
                        viewPagerBillers.currentItem = 1
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
            tabLayoutBillers.visibility = View.VISIBLE
            adapter?.addFragment(
                FrequentBillerFragment.newInstance(),
                FRAGMENT_FREQUENT_BILLERS
            )
        } else {
            tabLayoutBillers.visibility = View.GONE
        }
        adapter?.addFragment(
            AllBillerFragment.newInstance(),
            FRAGMENT_ALL_BILLERS
        )
        viewPagerBillers.offscreenPageLimit = 0
        viewPagerBillers.currentItem = 1
        viewPagerBillers.adapter = adapter
        tabLayoutBillers.setupWithViewPager(viewPagerBillers, false)
    }

    fun setCurrentViewPager(currentItem: Int) {
        viewPagerBillers.currentItem = currentItem
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
}
