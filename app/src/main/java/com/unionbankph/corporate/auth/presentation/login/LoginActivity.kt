package com.unionbankph.corporate.auth.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.databinding.ActivityLoginBinding
import com.unionbankph.corporate.settings.presentation.splash.SplashFrameActivity
import com.unionbankph.corporate.settings.presentation.splash.SplashStartedScreenActivity

class LoginActivity :
    BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        if (settingsUtil.isEmulator()) {
            showErrorAndExit(
                formatString(R.string.title_emulator_detected),
                formatString(R.string.msg_emulator_not_supported)
            )
        } else {
            if (!sharedPreferenceUtil.isLaunched().get()) {
                if (isSME) {
                    navigator.navigate(
                        this,
                        SplashStartedScreenActivity::class.java,
                        null,
                        isClear = true,
                        isAnimated = false
                    )
                } else {
                    navigator.navigate(
                        this,
                        SplashFrameActivity::class.java,
                        null,
                        isClear = true,
                        isAnimated = false
                    )
                }
            } else {
                navigator.replaceFragment(
                    R.id.fl_login,
                    LoginFragment(),
                    null,
                    supportFragmentManager,
                    "LoginFragment",
                    false
                )
            }
        }
    }

    private fun showErrorAndExit(
        title: String = formatString(R.string.title_error),
        message: String
    ) {
        MaterialDialog(this).show {
            lifecycleOwner(this@LoginActivity)
            title(text = title)
            cancelOnTouchOutside(false)
            message(text = message)
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                    finish()
                }
            )
        }
    }

    companion object {
        const val EXTRA_SPLASH_SCREEN = "splash_screen"
    }

    override val viewModelClassType: Class<LoginViewModel>
        get() = LoginViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding
        get() = ActivityLoginBinding::inflate
}
