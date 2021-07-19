package com.unionbankph.corporate.auth.presentation.onboarding_register

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

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