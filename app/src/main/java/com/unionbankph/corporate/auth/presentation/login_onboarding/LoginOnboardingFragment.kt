package com.unionbankph.corporate.auth.presentation.login_onboarding

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.runPostDelayed
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginFragment
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityOnboardingSignupBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import timber.log.Timber

class LoginOnboardingFragment :
    BaseFragment<ActivityOnboardingSignupBinding, LoginOnboardingViewModel>() {


    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Success -> {

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

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        initAnimationLogo()
        initFreshLogin()
    }


    fun initViews(){
        binding.btnOnboardingRegister.setOnClickListener {
            viewModel.onClickedStartLaunch()
            navigator.navigate(
                getAppCompatActivity(),
                OpenAccountActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )

        }

        binding.btnOnboardingLogin.setOnClickListener {
            viewModel.onClickedStartLaunch()
            viewUtil.startAnimateView(false, binding.constraintLayout, android.R.anim.fade_out)
            navigator.replaceFragment(
                R.id.frameLayoutSignup,
                LoginFragment(),
                null,
                childFragmentManager,
                "LoginFragment",
                false
            )
        }

    }

    private fun initFreshLogin(){
        runPostDelayed(
            {
                binding.bgSignupIllustration.visibility = View.VISIBLE
                binding.btnOnboardingRegister.visibility = View.VISIBLE
                binding.btnOnboardingLogin.visibility = View.VISIBLE
                binding.tvUbCaption.visibility = View.VISIBLE
                binding.flSignup.visibility = View.VISIBLE
            }, 100
        )

    }

    private fun initAnimationLogo() {
        binding.imageViewLogoOnboarding.root.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.imageViewLogoOnboarding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
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
                viewUtil.startAnimateView(true, binding.constraintLayout, android.R.anim.fade_in)
            }, 250
        )
        val location = IntArray(2)
        binding.imageViewLogoOnboarding.root.getLocationOnScreen(location)
        val y = location[1]
        val objectAnimator =
            ObjectAnimator.ofFloat(binding.imageViewLogoAnimateOnboarding, "y", y.toFloat())
        Timber.d("y axis:${y.toFloat()}")
        objectAnimator.duration = resources.getInteger(R.integer.anim_duration_medium).toLong()
        objectAnimator.start()
        runPostDelayed(
            {
                binding.imageViewLogoOnboarding.root.visibility(true)
                runPostDelayed(
                    {
                        binding.imageViewLogoAnimateOnboarding.root.visibility(false)
                    }, 50
                )
            }, 550
        )
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ActivityOnboardingSignupBinding
        get() = ActivityOnboardingSignupBinding::inflate

    override val viewModelClassType: Class<LoginOnboardingViewModel>
        get() = LoginOnboardingViewModel::class.java

}