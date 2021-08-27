package com.unionbankph.corporate.auth.presentation.onboarding_register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.databinding.ActivityOnboardingRegisterBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class OnboardingRegisterActivity :
    BaseActivity<ActivityOnboardingRegisterBinding, OnboardingRegisterViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

    }


    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    fun initViews(){
        RxView.clicks(binding.btnExistingUbAccount)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                gotoUserCreationForm()
            }.addTo(disposables)
    }

    private fun gotoUserCreationForm() {
        navigator.navigate(
            this,
            OpenAccountActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override val bindingInflater: (LayoutInflater) -> ActivityOnboardingRegisterBinding
        get() = ActivityOnboardingRegisterBinding::inflate

    override val viewModelClassType: Class<OnboardingRegisterViewModel>
        get() = OnboardingRegisterViewModel::class.java
}