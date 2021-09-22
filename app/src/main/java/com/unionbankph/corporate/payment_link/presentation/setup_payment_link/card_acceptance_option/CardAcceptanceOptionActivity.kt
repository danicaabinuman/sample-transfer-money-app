package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.databinding.ActivityCardAcceptanceOptionBinding
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.BusinessInformationActivity

class CardAcceptanceOptionActivity :
    BaseActivity<ActivityCardAcceptanceOptionBinding, CardAcceptanceOptionViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        selectYesCardPayments()
        selectNotNowCardPayments()
        selectDontAskMeAgain()
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

    private fun selectYesCardPayments(){
        binding.btnYes.setOnClickListener {
            navigator.navigate(
                this,
                YesAcceptCardPaymentsActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
    }

    private fun selectNotNowCardPayments(){
        binding.btnNotNow.setOnClickListener {
            navigator.navigate(
                this,
                NotNowCardPaymentsActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
    }

    private fun selectDontAskMeAgain(){
        binding.btnDontAskAgain.setOnClickListener {
            val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_ONBOARDED, Context.MODE_PRIVATE)
            if (sharedPref.getBoolean(SHAREDPREF_IS_ONBOARDED, false)){
                continueToNextScreen()
            } else {
                val editor = sharedPref.edit()
                editor.putBoolean(SHAREDPREF_IS_ONBOARDED, true)
                editor.apply()
            }
        }
    }

    private fun continueToNextScreen() {
        val intentGoBackToDashboard = Intent(this, NotNowCardPaymentsActivity::class.java)
        startActivity(intentGoBackToDashboard)
    }
    companion object {
        const val SHAREDPREF_IS_ONBOARDED = "sharedpref_is_onboarded"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityCardAcceptanceOptionBinding
        get() = ActivityCardAcceptanceOptionBinding::inflate
    override val viewModelClassType: Class<CardAcceptanceOptionViewModel>
        get() = CardAcceptanceOptionViewModel::class.java
}