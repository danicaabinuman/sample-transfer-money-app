package com.unionbankph.corporate.settings.presentation.learn_more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.URLDataEnum
import com.unionbankph.corporate.databinding.ActivityLearnMoreBinding
import com.unionbankph.corporate.settings.presentation.splash.SplashFrameActivity

class LearnMoreActivity :
    BaseActivity<ActivityLearnMoreBinding, LearnMoreViewModel>(),
    EpoxyAdapterCallback<LearnMoreData> {

    private val controller by lazyFast { LearnMoreController() }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_learn_more))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        binding.srlLearnMore.isEnabled = false
        initRecyclerView()
    }

    override fun onClickItem(view: View, data: LearnMoreData, position: Int) {
        when (data.id) {
            1 -> {
                navigator.navigateBrowser(
                    this,
                    URLDataEnum.ACCOUNT_OPENING_LINK
                )
            }
            2 -> {
                navigator.navigateBrowser(
                    this,
                    URLDataEnum.ENROLLMENT_LINK
                )
            }
            3 -> {
                navigateSplashScreen()
            }
            4 -> {
                navigatePrivacyPolicy()
            }
        }
    }

    private fun initViewModel() {
        viewModel.learnMoreData.observe(this, Observer {
            updateController(it)
        })
    }

    private fun navigatePrivacyPolicy() {
        val bundle = Bundle()
        bundle.putString(
            PrivacyPolicyActivity.EXTRA_REQUEST_PAGE, PrivacyPolicyActivity.PAGE_LEARN_MORE
        )
        navigator.navigate(
            this,
            PrivacyPolicyActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateSplashScreen() {
        navigator.navigate(
            this,
            SplashFrameActivity::class.java,
            Bundle().apply {
                putString(SplashFrameActivity.EXTRA_SCREEN, SplashFrameActivity.SCREEN_LEARN_MORE)
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
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

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        binding.rvLearnMore.setController(controller)
    }

    private fun updateController(data: MutableList<LearnMoreData>) {
        controller.setData(data)
    }

    override val viewModelClassType: Class<LearnMoreViewModel>
        get() = LearnMoreViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityLearnMoreBinding
        get() = ActivityLearnMoreBinding::inflate

}
