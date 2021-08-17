package com.unionbankph.corporate.auth.presentation.login_onboarding

import android.animation.ObjectAnimator
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.runPostDelayed
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import kotlinx.android.synthetic.main.activity_onboarding_signup.*
import timber.log.Timber

class LoginOnboardingFragment : BaseFragment<LoginOnboardingViewModel>(R.layout.activity_onboarding_signup) {


    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[LoginOnboardingViewModel::class.java]
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
        btn_onboarding_register.setOnClickListener {
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

        btn_onboarding_login.setOnClickListener {
            viewModel.onClickedStartLaunch()
        }

    }

    private fun initFreshLogin(){
        runPostDelayed(
            {
                bg_signup_illustration.visibility(true)
                btn_onboarding_register.visibility(true)
                btn_onboarding_login.visibility(true)
                tv_ub_caption.visibility(true)
                fl_signup.visibility(true)
            }, 100
        )

    }

    private fun initAnimationLogo() {
        imageViewLogoOnboarding.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    imageViewLogoOnboarding.viewTreeObserver.removeOnGlobalLayoutListener(this)
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
                viewUtil.startAnimateView(true, constraintLayout, android.R.anim.fade_in)
            }, 250
        )
        val location = IntArray(2)
        imageViewLogoOnboarding.getLocationOnScreen(location)
        val y = location[1]
        val objectAnimator =
            ObjectAnimator.ofFloat(imageViewLogoAnimateOnboarding, "y", y.toFloat())
        Timber.d("y axis:${y.toFloat()}")
        objectAnimator.duration = resources.getInteger(R.integer.anim_duration_medium).toLong()
        objectAnimator.start()
        runPostDelayed(
            {
                imageViewLogoOnboarding.visibility(true)
                runPostDelayed(
                    {
                        imageViewLogoAnimateOnboarding.visibility(false)
                    }, 50
                )
            }, 550
        )
    }

}