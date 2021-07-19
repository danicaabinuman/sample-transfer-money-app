package com.unionbankph.corporate.settings.presentation.splash

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralDismissLoading
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralLoading
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralSuccessSetUdid
import com.unionbankph.corporate.databinding.FragmentSplashScreenEndBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class SplashEndFragment :
    BaseFragment<FragmentSplashScreenEndBinding, GeneralViewModel>() {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        RxView.clicks(binding.buttonLogin)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewModel.onClickedLogin()
            }.addTo(disposables)
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralLoading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is ShowGeneralDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowGeneralSuccessSetUdid -> {
                    navigator.navigate(
                        getAppCompatActivity(),
                        LoginActivity::class.java,
                        null,
                        isClear = true,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                }
                is ShowGeneralError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override val layoutId: Int
        get() = R.layout.fragment_splash_screen_end

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java

}
