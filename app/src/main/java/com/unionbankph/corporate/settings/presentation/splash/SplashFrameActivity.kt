package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import kotlinx.android.synthetic.main.activity_splash_frame_screen.*

class SplashFrameActivity : BaseActivity<GeneralViewModel>(R.layout.activity_splash_frame_screen) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        setMargins(indicator, 0, 0, 0, getNavHeight())
        setMargins(
            textViewSkip,
            0,
            getStatusBarHeight() + resources.getDimension(R.dimen.content_spacing).toInt(),
            resources.getDimension(R.dimen.content_spacing).toInt(),
            0
        )
    }

    override fun onViewsBound() {
        imageViewBack.visibility =
            if (sharedPreferenceUtil.isLaunched().get())
                View.VISIBLE
            else
                View.GONE
        setupViewPager()
    }

    override fun onInitializeListener() {
        textViewSkip.setOnClickListener {
            viewPager.currentItem = 5
        }
        imageViewBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter.addFragment(
            SplashFragment.newInstance(SplashFragment.SplashScreenPage.PAGE_SAFE_FIRST),
            SplashFragment.SplashScreenPage.PAGE_SAFE_FIRST.name
        )
        adapter.addFragment(
            SplashFragment.newInstance(SplashFragment.SplashScreenPage.PAGE_TRANSFER),
            SplashFragment.SplashScreenPage.PAGE_TRANSFER.name
        )
        adapter.addFragment(
            SplashFragment.newInstance(SplashFragment.SplashScreenPage.PAGE_BILLS_PAYMENT),
            SplashFragment.SplashScreenPage.PAGE_BILLS_PAYMENT.name
        )
        adapter.addFragment(
            SplashFragment.newInstance(SplashFragment.SplashScreenPage.PAGE_APPROVAL),
            SplashFragment.SplashScreenPage.PAGE_APPROVAL.name
        )
        adapter.addFragment(
            SplashFragment.newInstance(SplashFragment.SplashScreenPage.PAGE_ORGANIZATION),
            SplashFragment.SplashScreenPage.PAGE_ORGANIZATION.name
        )
        if (sharedPreferenceUtil.isLaunched().get()) {
            textViewSkip.visibility = View.INVISIBLE
            viewPager?.offscreenPageLimit = 4
        } else {
            adapter.addFragment(
                SplashEndFragment(),
                SplashFragment.SplashScreenPage.PAGE_SUMMARY.name
            )
            viewPager?.offscreenPageLimit = 5
        }

        viewPager?.adapter = adapter

        indicator?.setViewPager(viewPager)
        viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

                if (position == 4 && imageViewBackground.visibility == View.VISIBLE) {
                    val defaultOffsetHide = 1f
                    val currentOffsetByAlpha = defaultOffsetHide - positionOffset
                    textViewSkip.alpha = currentOffsetByAlpha
                    imageViewBackground.alpha = currentOffsetByAlpha
                } else if (position == 4 && textViewSkip.visibility == View.INVISIBLE) {
                    if (!sharedPreferenceUtil.isLaunched().get()) {
                        textViewSkip.visibility = View.VISIBLE
                    }
                    textViewSkip.alpha = positionOffset
                    imageViewBackground.alpha = positionOffset
                }
                // onPageScrolled
            }

            override fun onPageSelected(position: Int) {
                // onPageSelected
                if (position == 5) {
                    textViewSkip.alpha = 0f
                    imageViewBackground.alpha = 0f
                    if (!sharedPreferenceUtil.isLaunched().get()) {
                        textViewSkip.visibility = View.INVISIBLE
                    }
                    textViewSkip.isEnabled = false
                } else {
                    if (!sharedPreferenceUtil.isLaunched().get()) {
                        textViewSkip.visibility = View.VISIBLE
                    }
                    textViewSkip.isEnabled = true
                }
                imageViewBack.visibility(position == 0)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // onPageScrollStateChanged
            }
        })
    }
}
