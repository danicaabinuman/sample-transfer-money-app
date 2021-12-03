package com.unionbankph.corporate.settings.presentation.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.runPostDelayed
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.login.LoginFragment
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivitySplashStartedScreenBinding

/**
 * Created by herald on 2/17/21
 */
class SplashStartedScreenActivity :
    BaseActivity<ActivitySplashStartedScreenBinding, SplashStartedScreenViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Success -> {
                    navigateOnboardingScreen()
                }
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.btnLetsGo.setOnClickListener {
            navigateOnboardingScreen()
            //viewModel.onClickedLetsGo()
        }
        binding.tvNoThanks.setOnClickListener {
            navigateLoginScreen()
            existingUser = 1
        }
    }

    private fun initViews(){
        if(isSME){
            initAnimationLogo()
        }else{
            binding.constraintLayoutContent.visibility = View.VISIBLE
            binding.imageViewBackground.visibility = View.VISIBLE
            binding.imageViewLogoAnimate.root.visibility = View.GONE
        }
    }

    private fun navigateOnboardingScreen(){
        navigator.navigate(
            this,
            SplashFrameOnboardingActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateLoginScreen(){
        navigator.replaceFragment(
            R.id.fl_wrapper,
            LoginFragment(),
            null,
            supportFragmentManager,
            "LoginFragment",
            false

        )
    }

    private fun initAnimationLogo() {
        binding.imageViewLogo.root.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.imageViewLogo.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    runPostDelayed(
                        {
                            animateContent()
                        }, 1000
                    )
                }
            }
        )
    }

    private fun animateContent() {
        runPostDelayed(
            {
                viewUtil.startAnimateView(true, binding.constraintLayoutContent, android.R.anim.fade_in)
            }, 400
        )
        val location = IntArray(2)
        binding.imageViewLogo.root.getLocationOnScreen(location)
        val y = location[1]
        val objectAnimator =
            ObjectAnimator.ofFloat(binding.imageViewLogoAnimate.root, "y", y.toFloat())
        objectAnimator.duration = resources.getInteger(R.integer.anim_duration_medium).toLong()
        objectAnimator.start()
        runPostDelayed(
            {
                binding.imageViewLogo.root.visibility = View.VISIBLE
                runPostDelayed(
                    {
                        binding.imageViewLogoAnimate.root.visibility = View.GONE
                    }, 100
                )
            }, 1000
        )
    }

    companion object{
        var existingUser = 0
    }

    override val viewModelClassType: Class<SplashStartedScreenViewModel>
        get() = SplashStartedScreenViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySplashStartedScreenBinding
        get() = ActivitySplashStartedScreenBinding::inflate

}