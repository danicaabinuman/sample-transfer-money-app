package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.auth.presentation.login.LoginFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.ActivitySplashOnboardingScreenBinding

class SplashFrameOnboardingActivity :
    BaseActivity<ActivitySplashOnboardingScreenBinding, GeneralViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        setMargins(
            binding.textViewSkip,
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
        binding.textViewSkip.setOnClickListener {
            binding.viewPager.currentItem = 5
        }
        binding.imageViewBack.setOnClickListener {
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
            binding.textViewSkip.visibility = View.INVISIBLE
            binding.viewPager?.offscreenPageLimit = 4
        } else {
            if (isSME) {
                adapter.addFragment(
                    LoginFragment(),
                    SplashFragment.SplashScreenPage.PAGE_LOGIN.name
                )
            } else {
                adapter.addFragment(
                    SplashEndFragment(),
                    SplashFragment.SplashScreenPage.PAGE_SUMMARY.name
                )
            }
            binding.viewPager?.offscreenPageLimit = 5
        }

        binding.viewPager?.adapter = adapter

        binding.indicator?.setViewPager(binding.viewPager)
        binding.viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

                if (position == 4 && binding.ivBgLightOrange.visibility == View.VISIBLE) {
                    val defaultOffsetHide = 1f
                    val currentOffsetByAlpha = defaultOffsetHide - positionOffset
                    binding.textViewSkip.alpha = currentOffsetByAlpha
                    binding.ivBgLightOrange.alpha = currentOffsetByAlpha
                } else if (position == 4 && binding.textViewSkip.visibility == View.INVISIBLE) {
                    binding.textViewSkip.visibility = View.VISIBLE
                    binding.textViewSkip.alpha = positionOffset
                    binding.ivBgLightOrange.alpha = positionOffset
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
        binding.ivBgLightOrange.visibility(isVisible)
        binding.textViewSkip.visibility(isVisible)
        binding.indicator.visibility(isVisible)
        binding.imageViewBack.visibility(isVisible)

    }


    companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_LEARN_MORE = "learn_more"
    }

    override val bindingInflater: (LayoutInflater) -> ActivitySplashOnboardingScreenBinding
        get() = ActivitySplashOnboardingScreenBinding::inflate

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}