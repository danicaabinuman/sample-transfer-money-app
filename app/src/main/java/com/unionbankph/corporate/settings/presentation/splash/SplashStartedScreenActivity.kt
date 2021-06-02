package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import kotlinx.android.synthetic.main.activity_splash_started_screen.*

/**
 * Created by herald on 2/17/21
 */
class SplashStartedScreenActivity :
    BaseActivity<SplashStartedScreenViewModel>(R.layout.activity_splash_started_screen) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[SplashStartedScreenViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Success -> {
                    navigator.navigate(
                        this,
                        SplashFrameActivity::class.java,
                        null,
                        isClear = true,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
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
        btn_lets_go.setOnClickListener {
            viewModel.onClickedLetsGo()
        }
        tv_no_thanks.setOnClickListener {
            onBackPressed()
        }
    }

}