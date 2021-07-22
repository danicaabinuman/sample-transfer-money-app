package com.unionbankph.corporate.mcd.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.databinding.ActivitySplashFrameScreenBinding
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class CheckDepositOnBoardingActivity :
    BaseActivity<ActivitySplashFrameScreenBinding, CheckDepositOnBoardingViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        setMargins(binding.indicator, 0, 0, 0, getNavHeight())
        setMargins(
            binding.textViewSkip,
            0,
            getStatusBarHeight() + resources.getDimension(R.dimen.content_spacing).toInt(),
            resources.getDimension(R.dimen.content_spacing).toInt(),
            0
        )
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        init()
    }

    private fun initBinding() {
        viewModel.isInitialLoad
            .subscribe {
                setupViewPager(it)
            }.addTo(disposables)
    }

    private fun init() {
        binding.imageViewBack.visibility(true)
        if (intent.getStringExtra(EXTRA_SCREEN) != null &&
            intent.getStringExtra(EXTRA_SCREEN) == CheckDepositScreenEnum.SUMMARY.name) {
            val bundle = Bundle().apply {
                putString(
                    CheckDepositCameraActivity.EXTRA_SCREEN,
                    CheckDepositScreenEnum.FRONT_OF_CHECK.name
                )
                putString(
                    CheckDepositCameraActivity.EXTRA_CHECK_DEPOSIT_TYPE,
                    CheckDepositTypeEnum.FRONT_OF_CHECK.name
                )
            }
            navigator.navigate(
                this,
                CheckDepositCameraActivity::class.java,
                bundle,
                isClear = true,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is ShowCheckDepositOnBoardingError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.isFirstCheckDeposit()
    }

    override fun onInitializeListener() {
        binding.textViewSkip.setOnClickListener {
            binding.viewPager.currentItem = 4
        }
        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewPager(isInitialLoad: Boolean) {
        val adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        if (!isInitialLoad) {
            adapter.addFragment(
                CheckDepositOnBoardingRemindersFragment(),
                CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.REMINDERS_SCREEN.name
            )
            binding.viewPager?.offscreenPageLimit = 0
            binding.textViewSkip.alpha = 0f
            binding.imageViewBackground.alpha = 0f
            binding.imageViewBackground.visibility = View.INVISIBLE
            binding.textViewSkip.visibility = View.INVISIBLE
            binding.indicator.visibility = View.GONE
            binding.textViewSkip.isEnabled = false
        } else {
            adapter.addFragment(
                CheckDepositOnBoardingScreenFragment.newInstance(
                    CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.CHECK_DEPOSIT_SCREEN
                ),
                CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.CHECK_DEPOSIT_SCREEN.name
            )
            adapter.addFragment(
                CheckDepositOnBoardingScreenFragment.newInstance(
                    CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.GET_TO_KNOW_SCREEN
                ),
                CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.GET_TO_KNOW_SCREEN.name
            )
            adapter.addFragment(
                CheckDepositOnBoardingScreenFragment.newInstance(
                    CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.FRONT_OF_CHECK_SCREEN
                ),
                CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.FRONT_OF_CHECK_SCREEN.name
            )
            adapter.addFragment(
                CheckDepositOnBoardingScreenFragment.newInstance(
                    CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.BACK_OF_CHECK_SCREEN
                ),
                CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.BACK_OF_CHECK_SCREEN.name
            )
            adapter.addFragment(
                CheckDepositOnBoardingRemindersFragment(),
                CheckDepositOnBoardingScreenFragment.CheckDepositOnBoardingScreenEnum.REMINDERS_SCREEN.name
            )
            binding.viewPager.offscreenPageLimit = 4
        }

        binding.viewPager.adapter = adapter
        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Timber.d("position: $position")
                Timber.d("positionOffset: $positionOffset")
                Timber.d("positionOffsetPixels: $positionOffsetPixels")
                if (viewModel.isInitialLoad.value.notNullable()) {
                    if (position == 3 && binding.imageViewBackground.visibility == View.VISIBLE) {
                        val defaultOffsetHide = 1f
                        val currentOffsetByAlpha = defaultOffsetHide - positionOffset
                        binding.textViewSkip.alpha = currentOffsetByAlpha
                        binding.imageViewBackground.alpha = currentOffsetByAlpha
                    } else if (position == 3 && binding.textViewSkip.visibility == View.INVISIBLE) {
                        binding.textViewSkip.visibility = View.VISIBLE
                        binding.textViewSkip.alpha = positionOffset
                        binding.imageViewBackground.alpha = positionOffset
                    }
                }
                // onPageScrolled
            }

            override fun onPageSelected(position: Int) {
                // onPageSelected
                if (viewModel.isInitialLoad.value.notNullable()) {
                    if (position == 4) {
                        binding.textViewSkip.alpha = 0f
                        binding.imageViewBackground.alpha = 0f
                        binding.textViewSkip.visibility = View.INVISIBLE
                        binding.indicator.visibility = View.GONE
                        binding.textViewSkip.isEnabled = false
                    } else {
                        binding.textViewSkip.visibility = View.VISIBLE
                        binding.indicator.visibility = View.VISIBLE
                        binding.textViewSkip.isEnabled = true
                    }
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
    }

    override val viewModelClassType: Class<CheckDepositOnBoardingViewModel>
        get() = CheckDepositOnBoardingViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySplashFrameScreenBinding
        get() = ActivitySplashFrameScreenBinding::inflate
}
