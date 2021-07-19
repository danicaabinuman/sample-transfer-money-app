package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.auth.presentation.login_onboarding.LoginOnboardingFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import kotlinx.android.synthetic.main.activity_splash_onboarding_screen.*

class SplashFrameOnboardingActivity : BaseActivity<GeneralViewModel>(R.layout.activity_splash_onboarding_screen) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        setMargins(
            textViewOnboardingSkip,
            0,
            getStatusBarHeight() + resources.getDimension(R.dimen.content_spacing).toInt(),
            resources.getDimension(R.dimen.content_spacing).toInt(),
            0
        )
    }

    override fun onViewsBound() {
        setupViewPager()
    }

    override fun onInitializeListener() {
        textViewOnboardingSkip.setOnClickListener {
            onboarding_viewPager.currentItem = 5
        }
        imageViewOnboardingBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter.addFragment(
            SplashOnboardingFragment.newInstance(SplashOnboardingFragment.SplashScreenPage.PAGE_VIEW_ACCOUNT),
            SplashOnboardingFragment.SplashScreenPage.PAGE_VIEW_ACCOUNT.name
        )
        adapter.addFragment(
            SplashOnboardingFragment.newInstance(SplashOnboardingFragment.SplashScreenPage.PAGE_TRANSFER_FUNDS),
            SplashOnboardingFragment.SplashScreenPage.PAGE_TRANSFER_FUNDS.name
        )
        adapter.addFragment(
            SplashOnboardingFragment.newInstance(SplashOnboardingFragment.SplashScreenPage.PAGE_PAY_BILLS),
            SplashOnboardingFragment.SplashScreenPage.PAGE_PAY_BILLS.name
        )
        adapter.addFragment(
            SplashOnboardingFragment.newInstance(SplashOnboardingFragment.SplashScreenPage.PAGE_PAYMENT_LINK),
            SplashOnboardingFragment.SplashScreenPage.PAGE_PAYMENT_LINK.name
        )
        adapter.addFragment(
            SplashOnboardingFragment.newInstance(SplashOnboardingFragment.SplashScreenPage.PAGE_DEPOSIT_CHECK),
            SplashOnboardingFragment.SplashScreenPage.PAGE_DEPOSIT_CHECK.name
        )
        if (intent.getStringExtra(EXTRA_SCREEN) == SCREEN_LEARN_MORE) {
            textViewOnboardingSkip.visibility = View.INVISIBLE
            onboarding_viewPager?.offscreenPageLimit = 4
        } else {
            if (isSME) {
                adapter.addFragment(
                    LoginOnboardingFragment(),
                    SplashFragment.SplashScreenPage.PAGE_LOGIN.name
                )
            } else {
                adapter.addFragment(
                    SplashEndFragment(),
                    SplashFragment.SplashScreenPage.PAGE_SUMMARY.name
                )
            }
            onboarding_viewPager?.offscreenPageLimit = 5
        }

        onboarding_viewPager?.adapter = adapter

        onboarding_indicator?.setViewPager(onboarding_viewPager)
        onboarding_viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

                if (position == 4 && iv_bg_light_orange.visibility == View.VISIBLE) {
                    val defaultOffsetHide = 1f
                    val currentOffsetByAlpha = defaultOffsetHide - positionOffset
                    textViewOnboardingSkip.alpha = currentOffsetByAlpha
                    iv_bg_light_orange.alpha = currentOffsetByAlpha
                } else if (position == 4 && textViewOnboardingSkip.visibility == View.INVISIBLE) {
                    textViewOnboardingSkip.visibility = View.VISIBLE
                    textViewOnboardingSkip.alpha = positionOffset
                    iv_bg_light_orange.alpha = positionOffset
                }
                // onPageScrolled
            }

            override fun onPageSelected(position: Int) {
                // onPageSelected
                if (position == 5) {
                    initViewVisibility(false)
                } else {
                    initViewVisibility(true)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // onPageScrollStateChanged
            }
        })
    }

    private fun initViewVisibility(isVisible: Boolean){
        iv_bg_light_orange.visibility(isVisible)
        textViewOnboardingSkip.visibility(isVisible)
        onboarding_indicator.visibility(isVisible)
        imageViewOnboardingBack.visibility(isVisible)

    }


    companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_LEARN_MORE = "learn_more"
    }

}