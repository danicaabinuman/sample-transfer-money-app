package com.unionbankph.corporate.trial_account.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.databinding.ActivityTrialAccountSetupBinding

class TrialAccountActivity :
    BaseActivity<ActivityTrialAccountSetupBinding, TrialAccountViewModel>()  {

    private var onBackPressedEvent: OnBackPressedEvent? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setToolbarTitle(binding.tvToolbar, formatString(R.string.title_cotinue_account_setup))
        setDrawableBackButton(R.drawable.ic_close_orange, R.color.colorDarkOrange, true)

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

    override fun onViewsBound() {
        super.onViewsBound()

        initOnClick()
    }

    private fun initOnClick(){
        binding.buttonNext.setOnClickListener { navigateDashboardScreen() }
    }

    private fun navigateDashboardScreen() {
        val bundle = Bundle().apply {
            putString(
                AutobahnFirebaseMessagingService.EXTRA_DATA,
                intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
            )
        }
        navigator.navigateClearStacks(
            this,
            DashboardActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override fun onBackPressed() {
        onBackPressedEvent?.onBackPressed()
        navigateDashboardScreen()
    }

    interface OnBackPressedEvent {
        fun onBackPressed() = Unit
    }

    fun setOnBackPressedEvent(onBackPressedEvent: OnBackPressedEvent) {
        this.onBackPressedEvent = onBackPressedEvent
    }

    override val bindingInflater: (LayoutInflater) -> ActivityTrialAccountSetupBinding
        get() = ActivityTrialAccountSetupBinding::inflate

    override val viewModelClassType: Class<TrialAccountViewModel>
        get() = TrialAccountViewModel::class.java
}