package com.unionbankph.corporate.mcd.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_splash_frame_screen.*
import timber.log.Timber

class CheckDepositOnBoardingActivity :
    BaseActivity<CheckDepositOnBoardingViewModel>(R.layout.activity_splash_frame_screen) {

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
        imageViewBack.visibility(true)
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
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[CheckDepositOnBoardingViewModel::class.java]
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
        textViewSkip.setOnClickListener {
            viewPager.currentItem = 4
        }
        imageViewBack.setOnClickListener {
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
            viewPager?.offscreenPageLimit = 0
            textViewSkip.alpha = 0f
            imageViewBackground.alpha = 0f
            imageViewBackground.visibility = View.INVISIBLE
            textViewSkip.visibility = View.INVISIBLE
            indicator.visibility = View.GONE
            textViewSkip.isEnabled = false
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
            viewPager?.offscreenPageLimit = 4
        }

        viewPager?.adapter = adapter
        indicator?.setViewPager(viewPager)
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Timber.d("position: $position")
                Timber.d("positionOffset: $positionOffset")
                Timber.d("positionOffsetPixels: $positionOffsetPixels")
                if (viewModel.isInitialLoad.value.notNullable()) {
                    if (position == 3 && imageViewBackground.visibility == View.VISIBLE) {
                        val defaultOffsetHide = 1f
                        val currentOffsetByAlpha = defaultOffsetHide - positionOffset
                        textViewSkip.alpha = currentOffsetByAlpha
                        imageViewBackground.alpha = currentOffsetByAlpha
                    } else if (position == 3 && textViewSkip.visibility == View.INVISIBLE) {
                        textViewSkip.visibility = View.VISIBLE
                        textViewSkip.alpha = positionOffset
                        imageViewBackground.alpha = positionOffset
                    }
                }
                // onPageScrolled
            }

            override fun onPageSelected(position: Int) {
                // onPageSelected
                if (viewModel.isInitialLoad.value.notNullable()) {
                    if (position == 4) {
                        textViewSkip.alpha = 0f
                        imageViewBackground.alpha = 0f
                        textViewSkip.visibility = View.INVISIBLE
                        indicator.visibility = View.GONE
                        textViewSkip.isEnabled = false
                    } else {
                        textViewSkip.visibility = View.VISIBLE
                        indicator.visibility = View.VISIBLE
                        textViewSkip.isEnabled = true
                    }
                }
                imageViewBack.visibility(position == 0)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // onPageScrollStateChanged
            }
        })
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
    }
}
