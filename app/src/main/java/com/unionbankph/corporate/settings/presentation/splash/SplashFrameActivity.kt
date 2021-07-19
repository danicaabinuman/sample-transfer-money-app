package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.auth.presentation.login.LoginFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.ActivitySplashFrameScreenBinding
import com.unionbankph.corporate.databinding.FragmentSplashScreenBinding

class SplashFrameActivity :
    BaseActivity<ActivitySplashFrameScreenBinding, GeneralViewModel>() {
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
        binding.imageViewBack.visibility =
            if (sharedPreferenceUtil.isLaunched().get())
                View.VISIBLE
            else
                View.GONE
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
        if (intent.getStringExtra(EXTRA_SCREEN) == SCREEN_LEARN_MORE) {
            binding.textViewSkip.visibility = View.INVISIBLE
            binding.viewPager.offscreenPageLimit = 4
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
            binding.viewPager.offscreenPageLimit = 5
        }

        binding.viewPager.adapter = adapter

        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

                if (position == 4 && binding.imageViewBackground.visibility == View.VISIBLE) {
                    val defaultOffsetHide = 1f
                    val currentOffsetByAlpha = defaultOffsetHide - positionOffset
                    binding.textViewSkip.alpha = currentOffsetByAlpha
                    binding.imageViewBackground.alpha = currentOffsetByAlpha
                } else if (position == 4 && binding.textViewSkip.visibility == View.INVISIBLE) {
                    if (!sharedPreferenceUtil.isLaunched().get()) {
                        binding.textViewSkip.visibility = View.VISIBLE
                    }
                    binding.textViewSkip.alpha = positionOffset
                    binding.imageViewBackground.alpha = positionOffset
                }
                // onPageScrolled
            }

            override fun onPageSelected(position: Int) {
                // onPageSelected
                if (position == 5) {
                    binding.textViewSkip.alpha = 0f
                    binding.imageViewBackground.alpha = 0f
                    if (!sharedPreferenceUtil.isLaunched().get()) {
                        binding.textViewSkip.visibility = View.INVISIBLE
                    }
                    binding.textViewSkip.isEnabled = false
                } else {
                    if (!sharedPreferenceUtil.isLaunched().get()) {
                        binding.textViewSkip.visibility = View.VISIBLE
                    }
                    binding.textViewSkip.isEnabled = true
                }
                binding.imageViewBack.visibility(position == 0)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // onPageScrollStateChanged
            }
        })
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_SPLASH = "splash"
        const val SCREEN_LEARN_MORE = "learn_more"
    }

    override val layoutId: Int
        get() = R.layout.activity_splash_frame_screen

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java

}
