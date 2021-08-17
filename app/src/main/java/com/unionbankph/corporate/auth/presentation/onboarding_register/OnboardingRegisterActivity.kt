package com.unionbankph.corporate.auth.presentation.onboarding_register

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_onboarding_register.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.toolbar
import java.util.concurrent.TimeUnit

class OnboardingRegisterActivity : BaseActivity<OnboardingRegisterViewModel>(R.layout.activity_onboarding_register) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[OnboardingRegisterViewModel::class.java]

    }


    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    fun initViews(){
        RxView.clicks(btn_existing_ub_account)
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
}